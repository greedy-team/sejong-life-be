package org.example.sejonglifebe.common.dto;

import org.example.sejonglifebe.place.entity.PlaceCategory;

public record CategoryInfo(
        Long categoryId,
        String categoryName
) {

    public static CategoryInfo from(PlaceCategory placeCategory) {
        return new CategoryInfo(placeCategory.getCategory().getId(), placeCategory.getCategory().getName());
    }
}
