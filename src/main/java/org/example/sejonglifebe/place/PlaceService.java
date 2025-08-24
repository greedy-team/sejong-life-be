package org.example.sejonglifebe.place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
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

    public PlaceDetailResponse getPlaceDetail(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));
        return PlaceDetailResponse.from(place);
    }
}
