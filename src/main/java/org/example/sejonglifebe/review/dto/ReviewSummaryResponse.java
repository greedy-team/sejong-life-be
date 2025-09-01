package org.example.sejonglifebe.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "리뷰 요약 정보")
public record ReviewSummaryResponse(
        @Schema(description = "리뷰 총 개수", example = "120") Long reviewCount,
        @Schema(description = "평균 별점", example = "4.3") Double averageRate,

        @Schema(description = "별점 분포(키: 별점, 값: 개수)", example = "{\"5\": 80, \"4\": 30, \"3\": 10}")
        Map<String, Long> ratingDistribution
) {

}
