package org.example.sejonglifebe.place;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceSearchConditions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Place", description = "장소")
public interface PlaceControllerSwagger {

    @Operation(summary = "장소 목록 조회")
    ResponseEntity<CommonResponse<List<PlaceResponse>>> getPlaces(
            @Valid @ModelAttribute PlaceSearchConditions conditions,
            AuthUser authUser);

    @Operation(summary = "주간 핫플레이스 조회")
    ResponseEntity<CommonResponse<List<PlaceResponse>>> getHotPlaces(AuthUser authUser);

    @Operation(summary = "장소 상세 정보 조회")
    ResponseEntity<CommonResponse<PlaceDetailResponse>> getPlaceDetail(
            @PathVariable Long placeId,
            AuthUser authUser,
            HttpServletRequest request);

    @Operation(summary = "장소 추가")
    ResponseEntity<CommonResponse<Void>> createPlace(
            @Valid @RequestPart("place") PlaceRequest placeRequest,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            AuthUser authUser
    );

    @Operation(summary = "장소 삭제")
    ResponseEntity<CommonResponse<Void>> deletePlace(
            @PathVariable Long placeId,
            AuthUser authUser
    );

    @Operation(summary = "내 즐겨찾기 목록 조회")
    ResponseEntity<CommonResponse<List<PlaceResponse>>> getMyFavoritePlaces(
            AuthUser authUser
    );

    @Operation(summary = "장소 즐겨찾기 추가")
    ResponseEntity<CommonResponse<Void>> addFavoritePlace(
            @PathVariable Long placeId,
            AuthUser authUser
    );

    @Operation(summary = "장소 즐겨찾기 삭제")
    ResponseEntity<CommonResponse<Void>> removeFavoritePlace(
            @PathVariable Long placeId,
            AuthUser authUser
    );

    @Operation(summary = "내 즐겨찾기 개수 조회")
    ResponseEntity<CommonResponse<Long>> getMyFavoriteCount(
            AuthUser authUser
    );
}
