package org.example.sejonglifebe.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MeetingOpenCountEventListener {

    private final MeetingOpenCountService meetingOpenCountService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCooldownStart(CooldownStartEvent event) {
        meetingOpenCountService.startCooldown(event.kakaoId());
    }
}
