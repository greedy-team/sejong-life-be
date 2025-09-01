package org.example.sejonglifebe.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "리뷰 작성 요청")
public record ReviewRequest(
        @Schema(description = "별점(1~5)", example = "5")
        @Min(value = 1, message = "별점은 1 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5 이하여야 합니다.")
        int rating,

        @Schema(description = "리뷰 내용", example = "분위기 좋고 커피가 맛있어요!")
        @NotBlank(message = "리뷰 내용은 필수 항목입니다.")
        String content,

        @Schema(description = "태그 ID 목록", example = "[1,2,3]")
        List<Long> tagIds
) {

}
