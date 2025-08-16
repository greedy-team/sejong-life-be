package org.example.sejonglifebe.place;

import java.util.List;
import lombok.AllArgsConstructor;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/api/places")
    public ResponseEntity<PlaceResponse> searchPlaces(
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "category", required = false) String category
    ) {
        PlaceRequest placeRequest = new PlaceRequest(tags, category);
        return ResponseEntity.ok(placeService.searchPlacesByFilter(placeRequest));
    }
}
