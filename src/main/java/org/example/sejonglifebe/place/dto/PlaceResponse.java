package org.example.sejonglifebe.place.dto;

import java.util.List;

public record PlaceResponse (
        String message,
        List<PlaceInfo> data) {

    public record PlaceInfo (
            Long placeId,
            String placeName,
            String mainImageUrl,
            List<CategoryInfo> categories,
            List<TagInfo> tags
    ) {}

    public record TagInfo(
            Long tagId,
            String tagName
    ) {}

    public record CategoryInfo(
            Long categoryId,
            String categoryName
    ) {}
}


