package org.example.sejonglifebe.place.dto;

import org.example.sejonglifebe.place.entity.Place;

public record PlaceQueryResult(
        Place place,
        Long reviewCount
) {
}
