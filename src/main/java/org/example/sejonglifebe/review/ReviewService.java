package org.example.sejonglifebe.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.example.sejonglifebe.review.dto.RatingCount;
import org.example.sejonglifebe.review.dto.ReviewResponse;
import org.example.sejonglifebe.review.dto.ReviewSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<ReviewResponse> getReviewsByPlaceId(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        List<Review> reviews = reviewRepository.findByPlace(place);

        return reviews.stream()
                .map(ReviewResponse::from)
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
    public void createReview(Long placeId, ReviewRequest reviewRequest, AuthUser authUser) {

        User user = userRepository.findByStudentId(authUser.studentId()).orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));
        Place place = placeRepository.findById(placeId).orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));
        List<Tag> tags = tagRepository.findByIdIn(reviewRequest.tagIds());
        if (tags.size() != reviewRequest.tagIds().size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }

        Review review = Review.createReview(place, user, reviewRequest.rating(), reviewRequest.content());
        if (!tags.isEmpty()) {
            tags.forEach(review::addTag);
        }

        reviewRepository.save(review);
        checkAndAddTagToPlace(tags, place);
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
