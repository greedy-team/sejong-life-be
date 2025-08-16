package org.example.sejonglifebe.place;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceResponse.CategoryInfo;
import org.example.sejonglifebe.place.dto.PlaceResponse.PlaceInfo;
import org.example.sejonglifebe.place.dto.PlaceResponse.TagInfo;
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

    public PlaceResponse searchPlacesByFilter(PlaceRequest placeRequest) {
        List<String> tagNames = placeRequest.tags();
        String categoryName = placeRequest.category();

        List<Tag> tags = tagRepository.findByNameIn(tagNames);
        Category category = categoryRepository.findByName(categoryName);

        List<Place> places = findPlacesByCategoryAndTags(category, tags);

        List<PlaceInfo> placeInfos = places.stream()
                .map(place -> new PlaceInfo(
                        place.getId(),
                        place.getName(),
                        place.getMainImageUrl(),
                        toCategoryInfos(place.getCategories()),
                        toTagInfos(place.getTags())))
                .toList();

        return new PlaceResponse("장소 목록 조회 성공",placeInfos);
    }

    private List<Place> findPlacesByCategoryAndTags(Category category, List<Tag> tags) {
        if (category.getName().equals("전체")) {
            return placeRepository.findByTags(tags);
        }
        return placeRepository.findPlacesByTagsAndCategory(category, tags);
    }

    private List<CategoryInfo> toCategoryInfos(List<Category> categories) {
        return categories.stream()
                .map(category -> new CategoryInfo(category.getId(), category.getName()))
                .toList();
    }

    private List<TagInfo> toTagInfos(List<Tag> tags) {
        return tags.stream()
                .map(tag -> new TagInfo(tag.getId(), tag.getName()))
                .toList();
    }

}
