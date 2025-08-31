package org.example.sejonglifebe.roulette;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.roulette.dto.RouletteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roulette")
@Tag(name = "Roulette", description = "룰렛 추천")
public class RouletteController {

    private final RouletteService rouletteService;

    @Operation(summary = "카테고리 기반 룰렛 추천")
    @GetMapping("/places/{categoryName}")
    public ResponseEntity<RouletteResponse> getRoulettePlaces(
            @PathVariable String categoryName) {
        return ResponseEntity.ok(rouletteService.getRoulettePlaces(categoryName));
    }
}
