package org.example.sejonglifebe.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
        String keyword
) {
}
