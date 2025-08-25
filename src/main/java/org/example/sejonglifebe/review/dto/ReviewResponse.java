package org.example.sejonglifebe.review.dto;

import java.util.List;
import org.example.sejonglifebe.place.entity.PlaceImage;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewTag;

public record ReviewResponse(
        Long reviewId,
        Long rating,
        String content,
        Long likeCount,
        String createdAt,
        List<String> images,
        List<TagInfo> tags

) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getLikeCount(),
                review.getCreatedAt().toString(),
                review.getPlaceImages().stream()
                        .map(PlaceImage::getUrl)
                        .toList(),
                review.getReviewTags().stream()
                        .map(TagInfo::from)
                        .toList()
        );
    }

    public record TagInfo(
            Long tagId,
            String tagName
    ) {
        public static TagInfo from(ReviewTag reviewTag) {
            return new TagInfo(
                    reviewTag.getTag().getId(),
                    reviewTag.getTag().getName()
            );
        }
    }
}
