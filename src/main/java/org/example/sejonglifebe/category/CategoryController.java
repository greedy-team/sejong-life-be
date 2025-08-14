package org.example.sejonglifebe.category;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.ApiResponse;
import org.example.sejonglifebe.category.dto.CategoryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categoryResponses = categoryService.findAll();
        return ApiResponse.of(HttpStatus.OK, "전체 카테고리 목록 조회 성공", categoryResponses);
    }
}
