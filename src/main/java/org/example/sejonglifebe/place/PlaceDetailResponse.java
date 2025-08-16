package org.example.sejonglifebe.place;

import java.util.List;
import java.util.stream.Collectors;

public record PlaceDetailResponse (
        Long id,
        String name,
        List<String> imageUrls,
        List<String> tags,
        MapLinks mapLinks
){
    public static PlaceDetailResponse from(Place place) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                place.getPlaceImages().stream()
                        .map(PlaceImage::getUrl)
                        .collect(Collectors.toList()),
                place.getPlaceTags().stream()
                        .map(placeTag -> placeTag.getTag().getName())
                        .collect(Collectors.toList()),
                place.getMapLinks()
        );
    }
}
