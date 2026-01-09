package org.example.sejonglifebe.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.sejonglifebe.category.dto.CategoryResponse;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Category", description = "카테고리")
public interface CategoryControllerSwagger {

    @Operation(summary = "카테고리 목록 조회")
    ResponseEntity<CommonResponse<List<CategoryResponse>>> getCategories();
}
