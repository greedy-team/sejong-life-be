package org.example.sejonglifebe.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.sejonglifebe.category.Category;

@Schema(description = "카테고리 정보")
public record CategoryResponse(
        @Schema(description = "카테고리 ID", example = "3")
        Long categoryId,

        @Schema(description = "카테고리명", example = "카페")
        String categoryName
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
