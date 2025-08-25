package org.example.sejonglifebe.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.RatingCount;
import org.example.sejonglifebe.review.dto.ReviewResponse;
import org.example.sejonglifebe.review.dto.ReviewSummaryResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PlaceRepository placeRepository;

    public List<ReviewResponse> getReviewsByPlaceId(String placeId) {
        Place place = placeRepository.findById(Long.parseLong(placeId))
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        List<Review> reviews = reviewRepository.findByPlace(place);

        return reviews.stream()
                .map(ReviewResponse::from)
                .toList();
    }

    public ReviewSummaryResponse getReviewSummaryByPlaceId(String placeId) {
        Place place = placeRepository.findById(Long.parseLong(placeId))
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        Long reviewCount = reviewRepository.countByPlace(place);
        Double averageRate = reviewRepository.averageRatingByPlace(place);

        List<RatingCount> ratingCounts = reviewRepository.findRatingCountsByPlace(place);

        Map<String, Long> ratingDistribution = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            ratingDistribution.put(String.valueOf(rating), 0L);
        }

        ratingCounts.forEach(rc -> ratingDistribution.put(String.valueOf(rc.rating()), rc.count()));

        return new ReviewSummaryResponse(reviewCount, averageRate, ratingDistribution);
    }
}
