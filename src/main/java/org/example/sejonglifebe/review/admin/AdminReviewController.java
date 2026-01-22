package org.example.sejonglifebe.review.admin;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.LoginRequired;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.user.Role;
import org.example.sejonglifebe.review.ReviewService;
import org.example.sejonglifebe.review.admin.dto.AdminReviewResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminReviewController {

    private final ReviewService reviewService;

    @LoginRequired(role = Role.ADMIN)
    @GetMapping("/reviews")
    public ResponseEntity<CommonResponse<List<AdminReviewResponse>>> getAdminReviews(AuthUser authUser) {
        return CommonResponse.of(HttpStatus.OK, "리뷰 로그 목록 조회 성공", reviewService.findAllReviews());
    }

    @LoginRequired
    @GetMapping(value = "/reviews/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamReviewLogs(AuthUser authUser) {
        String emitterId = "admin-" + authUser.studentId() + "-" + System.currentTimeMillis();
        return sseService.subscribe(emitterId);
    }
}
