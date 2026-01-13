package org.example.sejonglifebe.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.example.sejonglifebe.review.dto.ReviewResponse;
import org.example.sejonglifebe.review.dto.ReviewSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Review", description = "리뷰")
public interface ReviewControllerSwagger {

    @Operation(summary = "리뷰 목록 조회")
    ResponseEntity<CommonResponse<List<ReviewResponse>>> getReviews(
            @PathVariable("placeId") Long placeId,
            AuthUser authUser);

    @Operation(summary = "리뷰 요약 정보 조회")
    ResponseEntity<CommonResponse<ReviewSummaryResponse>> getReviewSummary(
            @PathVariable("placeId") Long placeId);

    @Operation(summary = "리뷰 작성")
    ResponseEntity<CommonResponse<Void>> createReview(
            @Parameter(description = "리뷰를 작성할 장소의 ID", required = true, example = "1")
            @PathVariable("placeId") Long placeId,

            @Valid
            @RequestPart("review")
            @Parameter(description = "리뷰 내용(JSON 형식)")
            ReviewRequest reviewRequest,

            @RequestPart(value = "images", required = false)
            @Parameter(description = "업로드할 이미지 파일 목록")
            List<MultipartFile> images,

            AuthUser authUser);

    @Operation(summary = "리뷰 수정")
    ResponseEntity<CommonResponse<Void>> updateReview(
            @PathVariable Long placeId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest reviewRequest,
            AuthUser authUser);

    @Operation(summary = "리뷰 삭제")
    ResponseEntity<CommonResponse<Void>> deleteReview(
            @PathVariable Long placeId,
            @PathVariable Long reviewId,
            AuthUser authUser);

    @Operation(summary = "리뷰 좋아요")
    ResponseEntity<CommonResponse<Void>> createLike(
            @PathVariable("reviewId") Long reviewId, AuthUser authUser);

    @Operation(summary = "리뷰 좋아요 취소")
    ResponseEntity<CommonResponse<Void>> deleteLike(
            @PathVariable("reviewId") Long reviewId, AuthUser authUser);
}
