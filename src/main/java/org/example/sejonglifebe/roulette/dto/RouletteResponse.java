package org.example.sejonglifebe.roulette.dto;

import org.example.sejonglifebe.place.entity.Place;

import java.util.List;
import java.util.stream.Collectors;

public record RouletteResponse(List<RoulettePlaceDto> places) {

    public static RouletteResponse from(List<Place> places) {
        List<RoulettePlaceDto> dtoList = places.stream()
                .map(place -> new RoulettePlaceDto(place.getId(), place.getName()))
                .collect(Collectors.toList());
        return new RouletteResponse(dtoList);
    }

    public record RoulettePlaceDto(Long placeId, String placeName) {
    }
}
