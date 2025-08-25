package org.example.sejonglifebe.place.dto;

import java.util.List;

import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.entity.PlaceCategory;
import org.example.sejonglifebe.place.entity.PlaceTag;

public record PlaceResponse(
        Long placeId,
        String placeName,
        String mainImageUrl,
        List<CategoryInfo> categories,
        List<TagInfo> tags
) {

    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getMainImageUrl(),
                place.getPlaceCategories().stream()
                        .map(CategoryInfo::from)
                        .toList(),
                place.getPlaceTags().stream()
                        .map(TagInfo::from)
                        .toList()
        );
    }

    public record TagInfo(
            Long tagId,
            String tagName
    ) {
        public static TagInfo from(PlaceTag placeTag) {
            return new TagInfo(placeTag.getTag().getId(), placeTag.getTag().getName());
        }
    }

    public record CategoryInfo(
            Long categoryId,
            String categoryName
    ) {
        public static CategoryInfo from(PlaceCategory placeCategory) {
            return new CategoryInfo(placeCategory.getCategory().getId(), placeCategory.getCategory().getName());
        }
    }
}


