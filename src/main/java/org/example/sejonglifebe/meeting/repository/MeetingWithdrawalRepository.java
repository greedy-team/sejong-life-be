package org.example.sejonglifebe.meeting.repository;

import org.example.sejonglifebe.meeting.entity.MeetingWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingWithdrawalRepository extends JpaRepository<MeetingWithdrawal, Long> {

    boolean existsByKakaoId(String kakaoId);
}
