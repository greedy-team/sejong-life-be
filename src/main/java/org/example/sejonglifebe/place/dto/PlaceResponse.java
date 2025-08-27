package org.example.sejonglifebe.place.dto;

import java.util.List;

import org.example.sejonglifebe.place.entity.Place;

public record PlaceResponse(
        Long placeId,
        String placeName,
        String thumbnailImageUrl,
        List<CategoryInfo> categories,
        List<TagInfo> tags
) {

    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getThumbnailImage(),
                place.getPlaceCategories().stream()
                        .map(CategoryInfo::from)
                        .toList(),
                place.getPlaceTags().stream()
                        .map(TagInfo::from)
                        .toList()
        );
    }
}
