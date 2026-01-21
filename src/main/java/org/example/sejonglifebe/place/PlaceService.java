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
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.common.dto.CategoryInfo;
import org.example.sejonglifebe.common.dto.TagInfo;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceSearchConditions;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.entity.PlaceImage;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;

    public List<PlaceResponse> getPlaceByConditions(PlaceSearchConditions conditions) {
        List<String> tagNames = conditions.tags();
        String categoryName = conditions.category();

        Category category = null;

        if (tagNames == null) {
            tagNames = Collections.emptyList();
        }

        List<Tag> tags = tagRepository.findByNameIn(tagNames);
        if (tags.size() != tagNames.size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }

        if (!categoryName.equals("전체")) {
            category = categoryRepository
                    .findByName(categoryName)
                    .orElseThrow(() -> new SejongLifeException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        return placeRepository.getPlacesByConditions(category, tags, conditions.keyword())
                .stream()
                .map(PlaceResponse::from)
                .toList();
    }

    @Transactional
    public void createPlace(PlaceRequest request, MultipartFile thumbnail, AuthUser authUser) {

        Place place = Place.createPlace(
                request.placeName(),
                request.address(),
                request.mapLinks(),
                request.isPartnership(),
                request.partnershipContent()
        );

        placeRepository.save(place);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String uploadedUrl = s3Service.uploadImage(place.getId(), thumbnail);
            place.addImage(uploadedUrl, true);
        }

        attachCategoriesToPlace(place, request);
        attachTagsToPlace(place, request);
    }

    @Transactional
    public void deletePlace(Long placeId, AuthUser authUser) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        List<PlaceImage> images = new ArrayList<>(place.getPlaceImages());
        s3Service.deleteImages(images);

        place.getPlaceImages().clear();

        List<Review> reviews = new ArrayList<>(place.getReviews());
        for (Review review : reviews) {
            review.getUser().removeReview(review);
        }
        place.getReviews().clear();

        placeRepository.delete(place);
    }

    @Transactional
    public PlaceDetailResponse getPlaceDetail(Long placeId, HttpServletRequest request, HttpServletResponse response) {
        increaseViewCount(placeId, request, response);
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));
        return PlaceDetailResponse.from(place);
    }

    @Transactional
    public List<PlaceResponse> getWeeklyHotPlaces() {
        List<Place> hotPlaces = placeRepository.findTop10ByOrderByWeeklyViewCountDesc();
        return hotPlaces.stream()
                .map(PlaceResponse::from).toList();
    }

    public void increaseViewCount(Long placeId, HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> placeViewCookie = extractCookie(request);

        boolean shouldIncreaseViewCount = placeViewCookie
                .map(cookie -> !cookie.getValue().contains("[" + placeId + "]"))
                .orElse(true);

        if (shouldIncreaseViewCount) {
            placeRepository.increaseViewCount(placeId);

            String existingValue = placeViewCookie.map(Cookie::getValue).orElse("");
            String updatedValue = existingValue.isEmpty() ? "[" + placeId + "]" : existingValue + "_[" + placeId + "]";

            ResponseCookie cookie = ResponseCookie.from("placeView", updatedValue)
                    .path("/")
                    .maxAge(60 * 60 * 6)
                    .secure(true)
                    .sameSite("None")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
        }
    }

    private static Optional<Cookie> extractCookie(HttpServletRequest request) {
        Optional<Cookie> placeViewCookie = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals("placeView"))
                        .findFirst());
        return placeViewCookie;
    }

    private void attachCategoriesToPlace(Place place, PlaceRequest request) {


        List<Category> categories = categoryRepository.findAllById(request.categoryIds());
        if (categories.size() != request.categoryIds().size()) {
            throw new SejongLifeException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        categories.forEach(place::addCategory);
    }

    private void attachTagsToPlace(Place place, PlaceRequest request) {

        List<Tag> tags = tagRepository.findAllById(request.tagIds());
        if (tags.size() != request.tagIds().size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }

        tags.forEach(place::addTag);
    }


}
