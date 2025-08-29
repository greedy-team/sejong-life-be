package org.example.sejonglifebe.review.dto;

import java.util.List;

import org.example.sejonglifebe.common.dto.TagInfo;
import org.example.sejonglifebe.place.entity.PlaceImage;
import org.example.sejonglifebe.review.Review;

public record ReviewResponse(
        Long reviewId,
        int rating,
        Long userId,
        String userName,
        String studentId,
        String content,
        Long likeCount,
        String createdAt,
        boolean liked,
        List<String> images,
        List<TagInfo> tags

) {

    public static ReviewResponse from(Review review,boolean liked) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getUser().getStudentId(),
                review.getContent(),
                review.getLikeCount(),
                review.getCreatedAt().toString(),
                liked,
                review.getPlaceImages().stream()
                        .map(PlaceImage::getUrl)
                        .toList(),
                review.getReviewTags().stream()
                        .map(rt -> new TagInfo(rt.getTag().getId(), rt.getTag().getName()))
                        .toList()
        );
    }
}
