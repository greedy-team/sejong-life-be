package org.example.sejonglifebe.place;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import java.util.List;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
@Tag(name = "Place API", description = "장소 API")
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "장소 목록 조회", description = "카테고리 및 태그 필터를 적용하여 장소 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PlaceResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 카테고리 또는 태그 조회",
                    content = @Content(schema = @Schema(implementation = PlaceResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<PlaceResponse>>> getPlaces(
            @Parameter(description = "필터링할 태그 목록", required = false)
            @RequestParam(value = "tags", required = false) List<String> tags,
            @Parameter(description = "조회할 카테고리", required = true)
            @RequestParam(value = "category") String category
    ) {
        PlaceRequest request = new PlaceRequest(tags, category);
        List<PlaceResponse> response = placeService.getPlacesFilteredByCategoryAndTags(request);
        return CommonResponse.of(HttpStatus.OK, "장소 목록 조회 성공", response);
    }

    @Operation(summary = "장소 상세 정보 조회", description = "placeId를 기반으로 특정 장소의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 상세 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = PlaceDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 장소 조회",
                    content = @Content(schema = @Schema(implementation = PlaceDetailResponse.class)))

    })
    @GetMapping("/{placeId}")
    public  ResponseEntity<CommonResponse<PlaceDetailResponse>> getPlaceDetail(
            @Parameter(description = "조회할 장소 ID", required = true)
            @PathVariable("placeId") Long placeId) {
        PlaceDetailResponse response = placeService.getPlaceDetail(placeId);
        return CommonResponse.of(HttpStatus.OK, "장소 상세 정보 조회 성공", response);
    }
}
