package org.example.sejonglifebe.review;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.example.sejonglifebe.review.dto.RatingCount;
import org.example.sejonglifebe.review.dto.ReviewResponse;
import org.example.sejonglifebe.review.dto.ReviewSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int RATING_MAX = 5;
    private static final int RATING_MIN = 1;
    private static final int TAG_THRESHOLD = 5;

    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final S3Service s3Service;

    public List<ReviewResponse> getReviewsByPlaceId(Long placeId, AuthUser authUser) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        List<Review> reviews = reviewRepository.findByPlace(place);
        Set<Long> likedReviewIds = (authUser == null)
                ? Collections.emptySet()
                : reviewLikeRepository.findByUserStudentId(authUser.studentId())
                .stream()
                .map(like -> like.getReview().getId())
                .collect(Collectors.toSet());

        return reviews.stream()
                .map(review -> {
                    boolean liked = likedReviewIds.contains(review.getId());
                    return ReviewResponse.from(review, liked);
                })
                .toList();
    }

    public ReviewSummaryResponse getReviewSummaryByPlaceId(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        Long reviewCount = reviewRepository.countByPlace(place);
        Double averageRate = reviewRepository.averageRatingByPlace(place);

        List<RatingCount> ratingCounts = reviewRepository.findRatingCountsByPlace(place);

        Map<String, Long> ratingDistribution = new HashMap<>();
        for (int rating = RATING_MIN; rating <= RATING_MAX; rating++) {
            ratingDistribution.put(String.valueOf(rating), 0L);
        }

        ratingCounts.forEach(rc -> ratingDistribution.put(String.valueOf(rc.rating()), rc.count()));

        return new ReviewSummaryResponse(reviewCount, averageRate, ratingDistribution);
    }

    @Transactional
    public void createReview(Long placeId, ReviewRequest reviewRequest, AuthUser authUser, List<MultipartFile> images) {
        User user = userRepository.findByStudentId(authUser.studentId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));
        List<Tag> tags = tagRepository.findByIdIn(reviewRequest.tagIds());
        if (tags.size() != reviewRequest.tagIds().size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }

        Review review = Review.createReview(place, user, reviewRequest.rating(), reviewRequest.content());
        if (!tags.isEmpty()) {
            tags.forEach(review::addTag);
        }

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String key = s3Service.uploadImage(placeId, image);
                review.addImage(key);
            }
        }

        reviewRepository.save(review);
        checkAndAddTagToPlace(tags, place);
    }

    @Transactional
    public void createLike(Long reviewId, AuthUser authUser) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.REVIEW_NOT_FOUND));
        User user = userRepository.findByStudentId(authUser.studentId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));

        if (reviewLikeRepository.existsByReviewAndUser(review, user)) {
            throw new SejongLifeException(ErrorCode.DUPLICATE_LIKE);
        }

        review.addLike(user);
        reviewRepository.incrementLikeCount(reviewId);
    }

    @Transactional
    public void deleteLike(Long reviewId, AuthUser authUser) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.REVIEW_NOT_FOUND));
        ReviewLike reviewLike = reviewLikeRepository.findByReviewIdAndUserStudentId(reviewId, authUser.studentId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.REVIEW_LIKE_NOT_FOUND));

        review.deleteReviewLike(reviewLike);
        reviewRepository.decrementLikeCount(reviewId);
    }

    private void checkAndAddTagToPlace(List<Tag> tags, Place place) {
        for (Tag tag : tags) {
            long count = reviewRepository.countByPlaceAndTag(place, tag);
            if (count >= TAG_THRESHOLD) {
                place.addTag(tag);
            }
        }
    }
}
