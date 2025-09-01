package org.example.sejonglifebe.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리 정보")
public record CategoryInfo(
        @Schema(description = "카테고리 ID", example = "3") Long categoryId,
        @Schema(description = "카테고리명", example = "카페") String categoryName
) {

}
