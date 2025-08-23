package org.example.sejonglifebe.review;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.RatingCount;
import org.example.sejonglifebe.review.dto.ReviewDataResponse;
import org.example.sejonglifebe.review.dto.ReviewResponse;
import org.example.sejonglifebe.review.dto.ReviewSummaryResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PlaceRepository placeRepository;

    public ReviewDataResponse getReviewDataByPlaceId(String placeId) {
        List<ReviewResponse> reviewResponses = getReviewsByPlaceId(placeId);
        ReviewSummaryResponse reviewSummaryResponse = getReviewSummaryByPlaceId(placeId);
        return new ReviewDataResponse(reviewSummaryResponse, reviewResponses);
    }

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

        Map<String, Long> ratingDistribution = ratingCounts.stream()
                .collect(Collectors.toMap(
                        ratingCount -> String.valueOf(ratingCount.rating().longValue()),
                        RatingCount::count
                ));
        return new ReviewSummaryResponse(reviewCount, averageRate, ratingDistribution);
    }
}
