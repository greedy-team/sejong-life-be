package org.example.sejonglifebe.place;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Place", description = "장소")
public interface PlaceControllerSwagger {

    @Operation(summary = "장소 목록 조회")
    ResponseEntity<CommonResponse<List<PlaceResponse>>> getPlaces(
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "category") String category);

    @Operation(summary = "주간 핫플레이스 조회")
    ResponseEntity<CommonResponse<List<PlaceResponse>>> getHotPlaces();

    @Operation(summary = "장소 상세 정보 조회")
    ResponseEntity<CommonResponse<PlaceDetailResponse>> getPlaceDetail(
            @PathVariable Long placeId,
            HttpServletRequest request,
            HttpServletResponse response);
}
