package org.example.sejonglifebe.review;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.ApiResponse;
import org.example.sejonglifebe.review.dto.ReviewResponse;
import org.example.sejonglifebe.review.dto.ReviewSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places/{placeId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @PathVariable("placeId") String placeId) {
        return ApiResponse.of(HttpStatus.OK, "리뷰 목록 조회 성공", reviewService.getReviewsByPlaceId(placeId));
    }

    @PostMapping(value = "/api/places/{placeId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> createReview(@PathVariable(name = "placeId") Long placeId,
                                                          @Valid @RequestPart("review") ReviewRequest reviewRequest,
                                                          @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                                          AuthUser authUser) {

        reviewService.createReview(placeId, reviewRequest, authUser);
        return ApiResponse.of(HttpStatus.CREATED, "리뷰 작성 성공", null);
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getReviewSummary(
            @PathVariable("placeId") String placeId) {
        return ApiResponse.of(HttpStatus.OK,"리뷰 요약 정보 조회 성공", reviewService.getReviewSummaryByPlaceId(placeId));
    }
}
