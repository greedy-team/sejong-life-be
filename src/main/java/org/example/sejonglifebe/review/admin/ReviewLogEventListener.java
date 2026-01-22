package org.example.sejonglifebe.review.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.review.admin.dto.AdminReviewResponse;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewLogEventListener {

    private final SseService sseService;

    @Async
    @EventListener
    public void handleReviewLogEvent(AdminReviewResponse reviewData) {
        log.info("리뷰 생성 이벤트 수신: reviewId={}, placeName={}", reviewData.reviewId(), reviewData.placeName());
        sseService.sendToAll("review-created", reviewData);
    }
}
