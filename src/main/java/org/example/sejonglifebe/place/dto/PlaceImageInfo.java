package org.example.sejonglifebe.place.dto;

import org.example.sejonglifebe.place.entity.PlaceImage;

public record PlaceImageInfo(
        Long imageId,
        String url
) {

    public static PlaceImageInfo from(PlaceImage placeImage) {
        return new PlaceImageInfo(placeImage.getId(), placeImage.getUrl());
    }
}
