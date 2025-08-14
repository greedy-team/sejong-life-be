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
    public ResponseEntity<PlaceResponse> searchPlaces(@RequestParam("tags") List<String> tags,
            @RequestParam("categories") List<String> categories
    ) {
        PlaceRequest placeRequest = new PlaceRequest(tags, categories);
        return ResponseEntity.ok(placeService.searchPlacesByFilter(placeRequest));
    }
}
