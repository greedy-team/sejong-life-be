package org.example.sejonglifebe.place;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.LoginRequired;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceSearchConditions;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController implements PlaceControllerSwagger {

    private final PlaceService placeService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<PlaceResponse>>> getPlaces(
            @Valid @ModelAttribute PlaceSearchConditions conditions) {
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

    @LoginRequired
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Void>> createPlace(
            @Valid @RequestPart("place") PlaceRequest placeRequest,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            AuthUser authUser
    ) {
        placeService.createPlace(placeRequest, thumbnail, authUser);
        return CommonResponse.of(HttpStatus.CREATED, "장소 추가 성공", null);
    }

    @LoginRequired
    @DeleteMapping("/{placeId}")
    public ResponseEntity<CommonResponse<Void>> deletePlace(
            @PathVariable Long placeId,
            AuthUser authUser
    ) {
        placeService.deletePlace(placeId, authUser);
        return CommonResponse.of(HttpStatus.OK, "장소 삭제 성공", null);
    }
}
