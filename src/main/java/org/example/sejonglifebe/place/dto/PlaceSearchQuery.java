package org.example.sejonglifebe.place.dto;

import lombok.Builder;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.tag.Tag;

import java.util.List;

@Builder
public record PlaceSearchQuery(
        Category category,
        List<Tag> tags,
        String keyword,
        boolean partnershipOnly,
        PlaceSortType sort,
        Double latitude,
        Double longitude
) {
    public PlaceSearchQuery {
        sort = PlaceSortType.orDefault(sort);
    }
}
