package org.example.sejonglifebe.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

@ParameterObject
@Schema(description = "장소 검색 조건")
public record PlaceSearchConditions(
        @Schema(description = "필터링할 태그 목록", example = "[\"혼밥하기 좋은\",\"가성비 좋은\"]")
        List<String> tags,

        @NotBlank
        @Schema(description = "조회할 카테고리", example = "카페")
        String category,

        @Schema(description = "검색어", example = "깍뚝")
        String keyword,

        @Schema(description = "제휴 장소 필터링 여부" , example = "false")
        boolean partnershipOnly,

        @Schema(description = "정렬 기준 (미지정 시 REVIEW_COUNT)", example = "REVIEW_COUNT")
        PlaceSortType sortType,

        @Schema(description = "사용자 위도 (거리순 정렬 시 필수)", example = "37.550838")
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @Schema(description = "사용자 경도 (거리순 정렬 시 필수)", example = "127.074430")
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude
) {
    public PlaceSearchConditions {
        sortType = PlaceSortType.orDefault(sortType);
    }

    @AssertTrue(message = "거리순 정렬 시 위도와 경도가 필요합니다.")
    @Schema(hidden = true)
    public boolean isLocationValidForDistanceSort() {
        return sortType != PlaceSortType.DISTANCE || (latitude != null && longitude != null);
    }
}
