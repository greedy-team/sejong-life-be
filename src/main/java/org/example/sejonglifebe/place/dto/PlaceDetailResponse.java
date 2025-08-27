package org.example.sejonglifebe.place.dto;

import java.util.List;

import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;

public record PlaceDetailResponse(
        Long id,
        String name,
        List<CategoryInfo> categories,
        List<PlaceImageInfo> images,
        List<TagInfo> tags,
        MapLinks mapLinks
) {

    public static PlaceDetailResponse from(Place place) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                place.getPlaceCategories().stream()
                        .map(CategoryInfo::from)
                        .toList(),
                place.getPlaceImages().stream()
                        .map(PlaceImageInfo::from)
                        .toList(),
                place.getPlaceTags().stream()
                        .map(TagInfo::from)
                        .toList(),
                place.getMapLinks()
        );
    }
}
