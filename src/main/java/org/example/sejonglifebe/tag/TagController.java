package org.example.sejonglifebe.tag;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.ApiResponse;
import org.example.sejonglifebe.tag.dto.TagResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping()
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags(@RequestParam(value = "categoryId", required = false) Long categoryId) {
        List<TagResponse> tagResponses = tagService.getTagsByCategoryId(categoryId);
        return ApiResponse.of(HttpStatus.OK, "전체 태그 목록 조회 성공", tagResponses);
    }

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getFrequentlyUsedTagsByCategoryId(@RequestParam(value = "categoryId") Long categoryId) {
        List<TagResponse> tagResponses = tagService.getFrequentlyUsedTagsByCategoryId(categoryId);
        return ApiResponse.of(HttpStatus.OK, "추천 태그 목록 조회 성공", tagResponses);
    }
}
