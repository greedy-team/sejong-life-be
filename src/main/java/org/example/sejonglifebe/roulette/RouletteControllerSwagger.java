package org.example.sejonglifebe.roulette;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.sejonglifebe.roulette.dto.RouletteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Roulette", description = "룰렛 추천")
public interface RouletteControllerSwagger {

    @Operation(summary = "카테고리 기반 룰렛 추천")
    ResponseEntity<RouletteResponse> getRoulettePlaces(
            @PathVariable String categoryName);
}
