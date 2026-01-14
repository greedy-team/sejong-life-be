package org.example.sejonglifebe.place;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
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
@Tag(name = "Place", description = "장소")
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "장소 목록 조회")
    @GetMapping
    public ResponseEntity<CommonResponse<List<PlaceResponse>>> getPlaces(
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "category") String category) {
        PlaceRequest request = new PlaceRequest(tags, category);
        List<PlaceResponse> response = placeService.getPlacesFilteredByCategoryAndTags(request);
        return CommonResponse.of(HttpStatus.OK, "장소 목록 조회 성공", response);
    }

    @Operation(summary = "주간 핫플레이스 조회")
    @GetMapping("/hot")
    public ResponseEntity<CommonResponse<List<PlaceResponse>>> getHotPlaces() {
        List<PlaceResponse> hotPlaceResponses = placeService.getWeeklyHotPlaces();
        return CommonResponse.of(HttpStatus.OK, "핫플레이스 조회 성공", hotPlaceResponses);
    }

    @Operation(summary = "장소 상세 정보 조회")
    @GetMapping("/{placeId}")
    public ResponseEntity<CommonResponse<PlaceDetailResponse>> getPlaceDetail(
            @PathVariable Long placeId,
            AuthUser authUser,
            HttpServletRequest request) {
        PlaceDetailResponse placeDetailResponse = placeService.getPlaceDetail(placeId, authUser, request);
        return CommonResponse.of(HttpStatus.OK, "장소 상세 정보 조회 성공", placeDetailResponse);
    }
}
