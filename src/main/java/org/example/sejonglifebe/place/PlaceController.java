package org.example.sejonglifebe.place;

import org.example.sejonglifebe.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    //장소 목록 필터링 및 조회 기능

    @GetMapping("/{placeId}")
    public  ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlaceDetail(@PathVariable Long placeId) {
        PlaceDetailResponse response = placeService.getPlaceDetail(placeId);
        return ApiResponse.of(HttpStatus.OK, "장소 상세 정보 조회 성공", response);
    }
}
