package org.example.sejonglifebe.meeting.repository;

import org.example.sejonglifebe.meeting.entity.Gender;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingProfileRepository extends JpaRepository<MeetingProfile, Long> {

    Optional<MeetingProfile> findByKakaoId(String kakaoId);

    boolean existsByKakaoId(String kakaoId);

    List<MeetingProfile> findByGender(Gender gender);
}
