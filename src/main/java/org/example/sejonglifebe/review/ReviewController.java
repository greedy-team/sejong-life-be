package org.example.sejonglifebe.review;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.ApiResponse;
import org.example.sejonglifebe.review.dto.ReviewResponse;
import org.example.sejonglifebe.review.dto.ReviewSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places/{placeId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @PathVariable("placeId") String placeId) {
        return ApiResponse.of(HttpStatus.OK,"리뷰 목록 조회 성공", reviewService.getReviewsByPlaceId(placeId));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getReviewSummary(
            @PathVariable("placeId") String placeId) {
        return ApiResponse.of(HttpStatus.OK,"리뷰 요약 정보 조회 성공", reviewService.getReviewSummaryByPlaceId(placeId));
    }

}

