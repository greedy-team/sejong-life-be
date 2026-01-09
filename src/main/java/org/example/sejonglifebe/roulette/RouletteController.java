package org.example.sejonglifebe.roulette;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.roulette.dto.RouletteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roulette")
public class RouletteController implements RouletteControllerSwagger {

    private final RouletteService rouletteService;

    @GetMapping("/places/{categoryName}")
    public ResponseEntity<RouletteResponse> getRoulettePlaces(
            @PathVariable String categoryName) {
        return ResponseEntity.ok(rouletteService.getRoulettePlaces(categoryName));
    }
}
