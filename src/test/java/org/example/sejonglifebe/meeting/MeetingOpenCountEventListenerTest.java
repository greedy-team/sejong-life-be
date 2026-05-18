package org.example.sejonglifebe.meeting;

import org.example.sejonglifebe.meeting.dto.MeetingAuthUser;
import org.example.sejonglifebe.meeting.entity.FaceType;
import org.example.sejonglifebe.meeting.entity.Gender;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.example.sejonglifebe.meeting.service.MeetingOpenCountService;
import org.example.sejonglifebe.meeting.service.MeetingProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MeetingOpenCountEventListenerTest {

    @Autowired
    private MeetingProfileService meetingProfileService;

    @Autowired
    private MeetingOpenCountService meetingOpenCountService;

    @Autowired
    private MeetingProfileRepository meetingProfileRepository;

    private static final String REQUESTER_KAKAO_ID = "kakao-event-test-1";
    private static final String TARGET_KAKAO_ID = "kakao-event-test-2";

    @BeforeEach
    void setUp() {
        MeetingProfile requester = MeetingProfile.builder()
                .kakaoId(REQUESTER_KAKAO_ID)
                .gender(Gender.MALE)
                .faceType(FaceType.DOG)
                .birthYear(2000)
                .hobby("축구")
                .dateStyle("활동적인 데이트")
                .contact("requester_contact")
                .bonusOpenCount(0)
                .build();

        MeetingProfile target = MeetingProfile.builder()
                .kakaoId(TARGET_KAKAO_ID)
                .gender(Gender.FEMALE)
                .faceType(FaceType.CAT)
                .birthYear(2001)
                .hobby("영화")
                .dateStyle("조용한 데이트")
                .contact("target_contact")
                .bonusOpenCount(0)
                .build();

        meetingProfileRepository.save(requester);
        meetingProfileRepository.save(target);
    }

    @AfterEach
    void tearDown() {
        meetingProfileRepository.findByKakaoId(REQUESTER_KAKAO_ID).ifPresent(meetingProfileRepository::delete);
        meetingProfileRepository.findByKakaoId(TARGET_KAKAO_ID).ifPresent(meetingProfileRepository::delete);
        meetingOpenCountService.clearCooldown(REQUESTER_KAKAO_ID);
    }

    @Test
    @DisplayName("openContact 트랜잭션 커밋 후 AFTER_COMMIT으로 Redis 쿨다운이 설정된다")
    void afterCommit_cooldown_is_set() {
        MeetingAuthUser meetingAuthUser = new MeetingAuthUser(REQUESTER_KAKAO_ID);
        MeetingProfile target = meetingProfileRepository.findByKakaoId(TARGET_KAKAO_ID).orElseThrow();

        assertThat(meetingOpenCountService.isRechargeable(REQUESTER_KAKAO_ID)).isTrue();

        meetingProfileService.openContact(meetingAuthUser, target.getId());

        assertThat(meetingOpenCountService.isRechargeable(REQUESTER_KAKAO_ID)).isFalse();
        assertThat(meetingOpenCountService.getRemainingCooldownSeconds(REQUESTER_KAKAO_ID)).isGreaterThan(0);
    }
}
