package org.example.sejonglifebe.place;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlaceSearchConditions;
import org.springframework.http.HttpStatus;

import java.util.List;

import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController implements PlaceControllerSwagger {

    private final PlaceService placeService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<PlaceResponse>>> getPlaces(
            @ModelAttribute PlaceSearchConditions conditions) {
        List<PlaceResponse> response = placeService.getPlaceByConditions(conditions);
        return CommonResponse.of(HttpStatus.OK, "장소 목록 조회 성공", response);
    }

    @GetMapping("/hot")
    public ResponseEntity<CommonResponse<List<PlaceResponse>>> getHotPlaces() {
        List<PlaceResponse> hotPlaceResponses = placeService.getWeeklyHotPlaces();
        return CommonResponse.of(HttpStatus.OK, "핫플레이스 조회 성공", hotPlaceResponses);
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<CommonResponse<PlaceDetailResponse>> getPlaceDetail(
            @PathVariable Long placeId,
            HttpServletRequest request,
            HttpServletResponse response) {
        PlaceDetailResponse placeDetailResponse = placeService.getPlaceDetail(placeId, request, response);
        return CommonResponse.of(HttpStatus.OK, "장소 상세 정보 조회 성공", placeDetailResponse);
    }
}
