package org.example.sejonglifebe.place.dto;

import java.util.List;

import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.entity.PlaceImage;

public record PlaceDetailResponse(
        Long id,
        String name,
        List<String> category,
        List<String> imageUrls,
        List<String> tags,
        MapLinks mapLinks
) {
    public static PlaceDetailResponse from(Place place) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                place.getPlaceCategories().stream()
                        .map(placeCategory -> placeCategory.getCategory().getName())
                        .toList(),
                place.getPlaceImages().stream()
                        .map(PlaceImage::getUrl)
                        .toList(),
                place.getPlaceTags().stream()
                        .map(placeTag -> placeTag.getTag().getName())
                        .toList(),
                place.getMapLinks()
        );
    }
}
