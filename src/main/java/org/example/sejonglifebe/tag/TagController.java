package org.example.sejonglifebe.tag;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        List<TagResponse> data = tagService.getAllTags();
        return ApiResponse.of(HttpStatus.OK, "전체 태그 목록 조회 성공", data);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTagsByCategoryId(@RequestParam("categoryId") Long categoryId) {
        List<TagResponse> tagResponses = tagService.getTagsByCategoryId(categoryId);
        return ApiResponse.of(HttpStatus.OK, "카테고리별 태그 목록 조회 성공", tagResponses);
    }
}
