package org.example.sejonglifebe.place.dto;

import org.example.sejonglifebe.common.dto.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record PlacePageResponse(
        List<PlaceResponse> places,
        PageResponse page
) {
    public static PlacePageResponse of(Page<PlaceResponse> page) {
        return new PlacePageResponse(
                page.getContent(),
                PageResponse.from(page)
        );
    }
}
