package org.example.sejonglifebe.place.dto;

import java.util.List;

import org.example.sejonglifebe.common.dto.CategoryInfo;
import org.example.sejonglifebe.common.dto.TagInfo;
import org.example.sejonglifebe.place.entity.Place;

public record PlaceResponse(
        Long placeId,
        String placeName,
        String thumbnailImageUrl,
        Long viewCount,
        Long reviewCount,
        List<CategoryInfo> categories,
        List<TagInfo> tags
) {

    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getThumbnailImage(),
                place.getViewCount(),
                (long) place.getReviews().size(),
                place.getPlaceCategories().stream()
                        .map(pc -> new CategoryInfo(pc.getCategory().getId(), pc.getCategory().getName()))
                        .toList(),
                place.getPlaceTags().stream()
                        .map(pt -> new TagInfo(pt.getTag().getId(), pt.getTag().getName()))
                        .toList()
        );
    }
}
