package org.example.sejonglifebe.meeting.repository;

import jakarta.persistence.LockModeType;
import org.example.sejonglifebe.meeting.entity.Gender;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MeetingProfileRepository extends JpaRepository<MeetingProfile, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MeetingProfile m WHERE m.kakaoId = :kakaoId")
    Optional<MeetingProfile> findByKakaoIdWithLock(String kakaoId);

    Optional<MeetingProfile> findByKakaoId(String kakaoId);

    boolean existsByKakaoId(String kakaoId);

    List<MeetingProfile> findByGender(Gender gender);
}
