package org.example.sejonglifebe.review.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.sejonglifebe.common.dto.TagInfo;
import org.example.sejonglifebe.review.Review;

import java.util.List;

public record AdminReviewResponse(
        @Schema(description = "리뷰 ID", example = "9001") Long reviewId,
        @Schema(description = "별점", example = "5") int rating,
        @Schema(description = "작성자 ID", example = "123") Long userId,
        @Schema(description = "작성자 닉네임", example = "카페덕후") String userName,
        @Schema(description = "작성자 학번", example = "21111111") String studentId,
        @Schema(description = "리뷰 내용", example = "커피가 진하고 맛있어요!") String content,
        @Schema(description = "좋아요 수", example = "7") Long likeCount,
        @Schema(description = "작성일시(ISO 문자열)", example = "2025-08-24T13:45:00") String createdAt,
        @Schema(description = "장소 ID", example = "1")Long placeId,
        @Schema(description = "장소 이름", example = "또래끼리")String placeName,
        @Schema(description = "이미지 개수") Long imagesCount,
        @Schema(description = "태그 목록") List<TagInfo> tags
) {

    public static AdminReviewResponse from(Review review) {
        return new AdminReviewResponse(
                review.getId(),
                review.getRating(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getUser().getStudentId(),
                review.getContent(),
                review.getLikeCount(),
                review.getCreatedAt().toString(),
                review.getPlace().getId(),
                review.getPlace().getName(),
                (long) review.getPlaceImages().size(),
                review.getReviewTags().stream()
                        .map(rt -> new TagInfo(rt.getTag().getId(), rt.getTag().getName()))
                        .toList()
        );
    }
}
