package org.example.sejonglifebe.review.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.review.admin.dto.AdminReviewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "AdminReview", description = "관리자 리뷰 로그")
public interface AdminReviewControllerSwagger {

    @Operation(summary = "관리자 리뷰 목록 전체 조회")
    ResponseEntity<CommonResponse<List<AdminReviewResponse>>> getAdminReviews(AuthUser authUser);

    @Operation(summary = "관리자 리뷰 로그 SSE 연결")
    public SseEmitter streamReviewLogs(AuthUser authUser);
}
