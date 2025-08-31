package org.example.sejonglifebe.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

@ParameterObject
@Schema(description = "장소 목록 조회 필터")
public record PlaceRequest(
        @Schema(description = "필터링할 태그 목록", example = "[\"혼밥하기 좋은\",\"가성비 좋은\"]")
        List<String> tags,

        @Schema(description = "조회할 카테고리", example = "카페")
        String category
) {

}
