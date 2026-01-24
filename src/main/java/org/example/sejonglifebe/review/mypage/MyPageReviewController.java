package org.example.sejonglifebe.review.mypage;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.LoginRequired;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.review.ReviewService;
import org.example.sejonglifebe.review.mypage.dto.MyPageReviewResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageReviewController {

    private final ReviewService reviewService;

    @LoginRequired
    @GetMapping("/reviews")
    public ResponseEntity<CommonResponse<List<MyPageReviewResponse>>> getMyPageReviews(AuthUser authUser) {
        List<MyPageReviewResponse> myPageReviews = reviewService.getMyPageReviews(authUser);
        return CommonResponse.of(HttpStatus.OK, "마이페이지 리뷰 목록 조회 성공", myPageReviews);
    }

    @LoginRequired
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<CommonResponse<Void>> deleteMyPageReview(
            @PathVariable Long reviewId,
            AuthUser authUser) {
        reviewService.deleteMyPageReview(reviewId, authUser);
        return CommonResponse.of(HttpStatus.OK, "마이페이지 리뷰 삭제 성공", null);
    }
}
