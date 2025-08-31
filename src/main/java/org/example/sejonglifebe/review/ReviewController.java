package org.example.sejonglifebe.review;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.LoginRequired;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.example.sejonglifebe.review.dto.ReviewResponse;
import org.example.sejonglifebe.review.dto.ReviewSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places/{placeId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<ReviewResponse>>> getReviews(
            @PathVariable("placeId") Long placeId,
            AuthUser authUser) {
        return CommonResponse.of(HttpStatus.OK, "리뷰 목록 조회 성공", reviewService.getReviewsByPlaceId(placeId, authUser));
    }

    @GetMapping("/summary")
    public ResponseEntity<CommonResponse<ReviewSummaryResponse>> getReviewSummary(
            @PathVariable("placeId") Long placeId) {
        return CommonResponse.of(HttpStatus.OK, "리뷰 요약 정보 조회 성공", reviewService.getReviewSummaryByPlaceId(placeId));
    }

    @LoginRequired
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Void>> createReview(@PathVariable("placeId") Long placeId,
                                                          @Valid @RequestPart("review") ReviewRequest reviewRequest,
                                                          @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                                          AuthUser authUser) {
        reviewService.createReview(placeId, reviewRequest, authUser);
        return CommonResponse.of(HttpStatus.CREATED, "리뷰 작성 성공", null);
    }

    @LoginRequired
    @PostMapping("/{reviewId}/likes")
    public ResponseEntity<CommonResponse<Void>> createlike(@PathVariable("reviewId") Long reviewId, AuthUser authUser) {
        reviewService.createLike(reviewId, authUser);
        return CommonResponse.of(HttpStatus.OK, "리뷰 좋아요 성공", null);
    }

    @LoginRequired
    @DeleteMapping("/{reviewId}/likes")
    public ResponseEntity<CommonResponse<Void>> deleteLike(@PathVariable("reviewId") Long reviewId, AuthUser authUser) {
        reviewService.deleteLike(reviewId, authUser);
        return CommonResponse.of(HttpStatus.OK, "리뷰 좋아요 취소 성공", null);
    }
}
