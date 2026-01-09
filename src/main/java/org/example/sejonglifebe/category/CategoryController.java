package org.example.sejonglifebe.category;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.category.dto.CategoryResponse;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController implements CategoryControllerSwagger {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categoryResponses = categoryService.getCategories();
        return CommonResponse.of(HttpStatus.OK, "전체 카테고리 목록 조회 성공", categoryResponses);
    }
}
