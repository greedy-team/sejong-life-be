package org.example.sejonglifebe.review.mypage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.review.mypage.dto.MyPageReviewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
@Tag(name = "MyPageReview", description = "마이페이지 리뷰")
public interface MyPageReviewControllerSwagger {

    @Operation(summary = "마이페이지 리뷰 목록 조회")
    ResponseEntity<CommonResponse<List<MyPageReviewResponse>>> getMyPageReviews(AuthUser authUser);

    @Operation(summary = "마이페이지 리뷰 삭제")
    ResponseEntity<CommonResponse<Void>> deleteMyPageReview(@PathVariable Long reviewId, AuthUser authUser);
}
