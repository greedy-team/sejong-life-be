package org.example.sejonglifebe.common.dto;

import org.example.sejonglifebe.place.entity.PlaceTag;

public record TagInfo(
        Long tagId,
        String tagName
) {

    public static TagInfo from(PlaceTag placeTag) {
        return new TagInfo(placeTag.getTag().getId(), placeTag.getTag().getName());
    }
}
