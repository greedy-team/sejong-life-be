package org.example.sejonglifebe.place;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.HotPlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;

    public List<PlaceResponse> getPlacesFilteredByCategoryAndTags(PlaceRequest placeRequest) {
        List<String> tagNames = placeRequest.tags();
        String categoryName = placeRequest.category();

        if (tagNames == null) {
            tagNames = Collections.emptyList();
        }
        List<Tag> tags = tagRepository.findByNameIn(tagNames);
        if (tags.size() != tagNames.size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }

        List<Place> places = new ArrayList<>();
        if (categoryName.equals("전체"))
            places = getPlacesByAllCategory(tags);

        Category category;
        if (!categoryName.equals("전체")) {
            category = categoryRepository
                    .findByName(categoryName)
                    .orElseThrow(() -> new SejongLifeException(ErrorCode.CATEGORY_NOT_FOUND));

            places = getPlacesBySelectedCategory(category, tags);
        }
        return places.stream().map(PlaceResponse::from).toList();
    }

    private List<Place> getPlacesByAllCategory(List<Tag> tags) {
        if (tags.isEmpty()) {
            return placeRepository.findAll();
        }
        return placeRepository.findByTags(tags);
    }

    private List<Place> getPlacesBySelectedCategory(Category category, List<Tag> tags) {
        if (tags.isEmpty()) {
            return placeRepository.findByCategory(category);
        }
        return placeRepository.findPlacesByTagsAndCategory(category, tags);
    }

    public PlaceDetailResponse getPlaceDetail(Long placeId, HttpServletRequest request, HttpServletResponse response) {
        increaseViewCount(placeId, request, response);
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));
        return PlaceDetailResponse.from(place);
    }

    public void increaseViewCount(Long placeId, HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> placeViewCookie = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals("placeView"))
                        .findFirst());

        boolean shouldIncreaseViewCount = placeViewCookie
                .map(cookie -> !cookie.getValue().contains("[" + placeId + "]"))
                .orElse(true);

        if (shouldIncreaseViewCount) {
            placeRepository.increaseViewCount(placeId);

            Cookie updatedCookie = placeViewCookie.orElseGet(() -> new Cookie("placeView", ""));

            String currentIds = updatedCookie.getValue();
            updatedCookie.setValue(currentIds.isEmpty() ? "[" + placeId + "]" : currentIds + "_[" + placeId + "]");
            updatedCookie.setPath("/");
            updatedCookie.setMaxAge(60 * 60 * 24);

            response.addCookie(updatedCookie);
        }
    }

    public List<HotPlaceResponse> getWeeklyHotPlaces() {
        List<Place> hotPlaces = placeRepository.findTop10ByOrderByWeeklyViewCountDesc();
        return hotPlaces.stream()
                .map(HotPlaceResponse::from).toList();
    }
}
