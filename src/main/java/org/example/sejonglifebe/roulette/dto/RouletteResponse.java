package org.example.sejonglifebe.roulette.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.sejonglifebe.place.entity.Place;

import java.util.List;

@Schema(description = "룰렛 추천 응답")
public record RouletteResponse(
        @Schema(description = "추천 장소 목록") List<RoulettePlaceDto> places
) {

    public static RouletteResponse from(List<Place> places) {
        List<RoulettePlaceDto> dtoList = places.stream()
                .map(place -> new RoulettePlaceDto(place.getId(), place.getName()))
                .toList();
        return new RouletteResponse(dtoList);
    }

    @Schema(description = "룰렛 추천 항목")
    public record RoulettePlaceDto(Long placeId, String placeName) {
    }
}
