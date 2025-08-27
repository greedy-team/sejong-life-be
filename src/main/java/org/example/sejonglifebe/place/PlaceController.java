package org.example.sejonglifebe.place;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.ApiResponse;
import org.example.sejonglifebe.place.dto.HotPlaceResponse;
import org.springframework.http.HttpStatus;

import java.util.List;

import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> getPlaces(
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "category") String category
    ) {
        PlaceRequest request = new PlaceRequest(tags, category);
        List<PlaceResponse> response = placeService.getPlacesFilteredByCategoryAndTags(request);
        return ApiResponse.of(HttpStatus.OK, "장소 목록 조회 성공", response);
    }

    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<HotPlaceResponse>>> getHotPlaces() {
        List<HotPlaceResponse> hotPlaceResponses = placeService.getWeeklyHotPlaces();
        return ApiResponse.of(HttpStatus.OK, "핫플레이스 조회 성공", hotPlaceResponses);
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlaceDetail(@PathVariable Long placeId,
                                                                           HttpServletRequest request,
                                                                           HttpServletResponse response)
    {
        PlaceDetailResponse placeDetailResponse =
                placeService.getPlaceDetail(placeId, request, response);
        return ApiResponse.of(HttpStatus.OK, "장소 상세 정보 조회 성공", placeDetailResponse);
    }

}
