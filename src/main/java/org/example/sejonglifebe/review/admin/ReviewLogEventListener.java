package org.example.sejonglifebe.review.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.review.admin.dto.AdminReviewResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewLogEventListener {

    private final SseService sseService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewLogEvent(AdminReviewResponse reviewData) {
        log.info("리뷰 생성 이벤트 수신: reviewId={}, placeName={}", reviewData.reviewId(), reviewData.placeName());
        sseService.sendToAll("review-created", reviewData);
    }
}
