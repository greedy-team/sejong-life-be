package org.example.sejonglifebe.review.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 리뷰 장소 정보")
public record PlaceSimpleInfo(
        @Schema(description = "장소 ID", example = "1") Long placeId,
        @Schema(description = "장소 이름", example = "또래끼리") String placeName
) {
}
