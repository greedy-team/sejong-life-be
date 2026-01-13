package org.example.sejonglifebe.tag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.tag.dto.TagResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Tag API", description = "태그 API")
public interface TagControllerSwagger {

    @Operation(summary = "태그 목록 조회", description = "카테고리별 태그 목록을 조회합니다.")
    ResponseEntity<CommonResponse<List<TagResponse>>> getTags(
            @Parameter(description = "조회할 카테고리 ID", required = false)
            @RequestParam(value = "categoryId", required = false) Long categoryId);

    @Operation(summary = "추천 태그 목록 조회", description = "해당 장소의 카테고리에서 많이 사용된 태그를 조회합니다.")
    ResponseEntity<CommonResponse<List<TagResponse>>> getFrequentlyUsedTagsByCategoryId(@RequestParam(value = "categoryId") Long categoryId);

}
