package org.example.sejonglifebe.tag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.CommonResponse;
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
@Tag(name = "Tag API", description = "태그 API")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "태그 목록 조회", description = "카테고리별 태그 목록을 조회합니다.")
    @GetMapping()
    public ResponseEntity<CommonResponse<List<TagResponse>>> getTags(
            @Parameter(description = "조회할 카테고리 ID", required = false)
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        List<TagResponse> tagResponses = tagService.getTagsByCategoryId(categoryId);
        return CommonResponse.of(HttpStatus.OK, "전체 태그 목록 조회 성공", tagResponses);
    }

    @GetMapping("/recommended")
    public ResponseEntity<CommonResponse<List<TagResponse>>> getFrequentlyUsedTagsByCategoryId(@RequestParam(value = "categoryId") Long categoryId) {
        List<TagResponse> tagResponses = tagService.getFrequentlyUsedTagsByCategoryId(categoryId);
        return CommonResponse.of(HttpStatus.OK, "추천 태그 목록 조회 성공", tagResponses);
    }
}
