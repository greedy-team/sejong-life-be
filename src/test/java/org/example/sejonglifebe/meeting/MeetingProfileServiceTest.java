package org.example.sejonglifebe.meeting;

import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.meeting.dto.MeetingContactResponse;
import org.example.sejonglifebe.meeting.dto.MeetingAuthUser;
import org.example.sejonglifebe.meeting.dto.MeetingOpenCountResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileCountResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileUpdateRequest;
import org.example.sejonglifebe.meeting.entity.FaceType;
import org.example.sejonglifebe.meeting.entity.Gender;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.ContactViewHistoryRepository;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.example.sejonglifebe.meeting.service.CooldownStartEvent;
import org.example.sejonglifebe.meeting.service.MeetingOpenCountService;
import org.example.sejonglifebe.meeting.service.MeetingProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeetingProfileServiceTest {

    @Mock
    private MeetingProfileRepository meetingProfileRepository;

    @Mock
    private ContactViewHistoryRepository contactViewHistoryRepository;

    @Mock
    private MeetingOpenCountService meetingOpenCountService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MeetingProfileService meetingProfileService;

    @Nested
    @DisplayName("프로필 수 조회")
    class GetProfileCountTest {

        @Test
        @DisplayName("남/녀/전체 프로필 수를 반환한다")
        void getProfileCount_success() {
            given(meetingProfileRepository.countByGender(Gender.MALE)).willReturn(80L);
            given(meetingProfileRepository.countByGender(Gender.FEMALE)).willReturn(70L);

            MeetingProfileCountResponse result = meetingProfileService.getProfileCount();

            assertThat(result.total()).isEqualTo(150L);
            assertThat(result.male()).isEqualTo(80L);
            assertThat(result.female()).isEqualTo(70L);
        }
    }

    @Nested
    @DisplayName("전체 조회")
    class GetAllMeetingProfilesTest {

        @Test
        @DisplayName("전체 미팅 프로필을 조회한다")
        void getAllMeetingProfiles_success() {
            MeetingProfile profile1 = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("contact1")
                    .build();

            MeetingProfile profile2 = MeetingProfile.builder()
                    .kakaoId("kakao-2")
                    .gender(Gender.FEMALE)
                    .faceType(FaceType.CAT)
                    .birthYear(2001)
                    .hobby("영화")
                    .dateStyle("조용한 데이트")
                    .contact("contact2")
                    .build();

            ReflectionTestUtils.setField(profile1, "id", 1L);
            ReflectionTestUtils.setField(profile2, "id", 2L);
            ReflectionTestUtils.setField(profile1, "createdAt", LocalDateTime.of(2026, 3, 31, 10, 0));
            ReflectionTestUtils.setField(profile2, "createdAt", LocalDateTime.of(2026, 3, 31, 11, 0));

            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-1");

            given(meetingProfileRepository.findByKakaoId("kakao-1")).willReturn(Optional.of(profile1));
            given(meetingProfileRepository.findByGender(Gender.FEMALE)).willReturn(List.of(profile2));

            List<MeetingProfileResponse> result = meetingProfileService.getAllMeetingProfiles(meetingAuthUser);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(2L);
            assertThat(result.get(0).kakaoId()).isEqualTo("kakao-2");
            assertThat(result.get(0).gender()).isEqualTo("FEMALE");
            assertThat(result.get(0).faceType()).isEqualTo("CAT");
        }
    }

    @Nested
    @DisplayName("수정")
    class UpdateMeetingProfileTest {

        @Test
        @DisplayName("미팅 프로필을 정상적으로 수정한다")
        void updateMeetingProfile_success() {
            MeetingProfile profile = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("contact1")
                    .build();

            ReflectionTestUtils.setField(profile, "id", 1L);
            ReflectionTestUtils.setField(profile, "createdAt", LocalDateTime.of(2026, 3, 31, 10, 0));

            MeetingProfileUpdateRequest request = new MeetingProfileUpdateRequest(
                    Gender.FEMALE,
                    FaceType.FOX,
                    1999,
                    "산책",
                    "카페 데이트",
                    "updated_contact"
            );

            given(meetingProfileRepository.findById(1L)).willReturn(Optional.of(profile));

            meetingProfileService.updateMeetingProfile(1L, request);

            assertThat(profile.getGender()).isEqualTo(Gender.FEMALE);
            assertThat(profile.getFaceType()).isEqualTo(FaceType.FOX);
            assertThat(profile.getBirthYear()).isEqualTo(1999);
            assertThat(profile.getHobby()).isEqualTo("산책");
            assertThat(profile.getDateStyle()).isEqualTo("카페 데이트");
            assertThat(profile.getContact()).isEqualTo("updated_contact");

            assertThat(profile.getKakaoId()).isEqualTo("kakao-1");
            assertThat(profile.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 3, 31, 10, 0));
        }

        @Test
        @DisplayName("존재하지 않는 미팅 프로필 수정 시 예외를 던진다")
        void updateMeetingProfile_fail_notFound() {
            MeetingProfileUpdateRequest request = new MeetingProfileUpdateRequest(
                    Gender.FEMALE,
                    FaceType.FOX,
                    1999,
                    "산책",
                    "카페 데이트",
                    "updated_contact"
            );

            given(meetingProfileRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> meetingProfileService.updateMeetingProfile(999L, request))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.MEETING_PROFILE_NOT_FOUND.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("연락처 열람")
    class OpenContactTest {

        @Test
        @DisplayName("쿨다운이 끝났을 때 연락처를 반환하고 쿨다운을 시작한다")
        void openContact_success() {
            MeetingProfile requester = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("requester_contact")
                    .bonusOpenCount(0)
                    .build();

            MeetingProfile target = MeetingProfile.builder()
                    .kakaoId("kakao-2")
                    .gender(Gender.FEMALE)
                    .faceType(FaceType.CAT)
                    .birthYear(2001)
                    .hobby("영화")
                    .dateStyle("조용한 데이트")
                    .contact("insta_contact")
                    .bonusOpenCount(0)
                    .build();

            ReflectionTestUtils.setField(requester, "id", 1L);
            ReflectionTestUtils.setField(target, "id", 2L);

            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-1");

            given(meetingProfileRepository.findByKakaoIdWithLock("kakao-1")).willReturn(Optional.of(requester));
            given(meetingProfileRepository.findById(2L)).willReturn(Optional.of(target));
            given(contactViewHistoryRepository.existsByViewerIdAndTargetId(1L, 2L)).willReturn(false);
            given(meetingOpenCountService.isRechargeable("kakao-1")).willReturn(true);

            MeetingContactResponse result = meetingProfileService.openContact(meetingAuthUser, 2L);

            assertThat(result.contact()).isEqualTo("insta_contact");
            assertThat(result.alreadyViewed()).isFalse();

            ArgumentCaptor<CooldownStartEvent> captor = ArgumentCaptor.forClass(CooldownStartEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().kakaoId()).isEqualTo("kakao-1");
        }

        @Test
        @DisplayName("쿨다운 중이고 보너스 열람권이 있을 때 보너스 열람권을 차감한다")
        void openContact_success_bonusUsedDuringCooldown() {
            MeetingProfile requester = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("requester_contact")
                    .bonusOpenCount(1)
                    .build();

            MeetingProfile target = MeetingProfile.builder()
                    .kakaoId("kakao-2")
                    .gender(Gender.FEMALE)
                    .faceType(FaceType.CAT)
                    .birthYear(2001)
                    .hobby("영화")
                    .dateStyle("조용한 데이트")
                    .contact("insta_contact")
                    .bonusOpenCount(0)
                    .build();

            ReflectionTestUtils.setField(requester, "id", 1L);
            ReflectionTestUtils.setField(target, "id", 2L);

            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-1");

            given(meetingProfileRepository.findByKakaoIdWithLock("kakao-1")).willReturn(Optional.of(requester));
            given(meetingProfileRepository.findById(2L)).willReturn(Optional.of(target));
            given(contactViewHistoryRepository.existsByViewerIdAndTargetId(1L, 2L)).willReturn(false);
            given(meetingOpenCountService.isRechargeable("kakao-1")).willReturn(false);

            meetingProfileService.openContact(meetingAuthUser, 2L);

            assertThat(requester.getBonusOpenCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("열람권이 없고 쿨다운 중이면 예외를 던진다")
        void openContact_fail_insufficientOpenCount() {
            MeetingProfile requester = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("requester_contact")
                    .bonusOpenCount(0)
                    .build();

            ReflectionTestUtils.setField(requester, "id", 1L);

            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-1");

            given(meetingProfileRepository.findByKakaoIdWithLock("kakao-1")).willReturn(Optional.of(requester));
            given(contactViewHistoryRepository.existsByViewerIdAndTargetId(1L, 2L)).willReturn(false);
            given(meetingProfileRepository.findById(2L)).willReturn(Optional.of(MeetingProfile.builder()
                    .kakaoId("kakao-2").gender(Gender.FEMALE).faceType(FaceType.CAT)
                    .birthYear(2001).hobby("영화").dateStyle("조용한 데이트").contact("insta_contact")
                    .bonusOpenCount(0).build()));
            given(meetingOpenCountService.isRechargeable("kakao-1")).willReturn(false);

            assertThatThrownBy(() -> meetingProfileService.openContact(meetingAuthUser, 2L))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.INSUFFICIENT_OPEN_COUNT.getErrorMessage());
        }

        @Test
        @DisplayName("이미 열람한 프로필은 열람권 차감 없이 연락처를 반환한다")
        void openContact_alreadyViewed() {
            MeetingProfile requester = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("requester_contact")
                    .bonusOpenCount(0)
                    .build();

            MeetingProfile target = MeetingProfile.builder()
                    .kakaoId("kakao-2")
                    .gender(Gender.FEMALE)
                    .faceType(FaceType.CAT)
                    .birthYear(2001)
                    .hobby("영화")
                    .dateStyle("조용한 데이트")
                    .contact("insta_contact")
                    .bonusOpenCount(0)
                    .build();

            ReflectionTestUtils.setField(requester, "id", 1L);
            ReflectionTestUtils.setField(target, "id", 2L);

            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-1");

            given(meetingProfileRepository.findByKakaoIdWithLock("kakao-1")).willReturn(Optional.of(requester));
            given(meetingProfileRepository.findById(2L)).willReturn(Optional.of(target));
            given(contactViewHistoryRepository.existsByViewerIdAndTargetId(1L, 2L)).willReturn(true);

            MeetingContactResponse result = meetingProfileService.openContact(meetingAuthUser, 2L);

            assertThat(result.contact()).isEqualTo("insta_contact");
            assertThat(result.alreadyViewed()).isTrue();
            verify(eventPublisher, org.mockito.Mockito.never()).publishEvent(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("자신의 프로필 열람 시 예외를 던진다")
        void openContact_fail_selfProfile() {
            MeetingProfile requester = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("requester_contact")
                    .bonusOpenCount(0)
                    .build();

            ReflectionTestUtils.setField(requester, "id", 1L);

            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-1");

            given(meetingProfileRepository.findByKakaoIdWithLock("kakao-1")).willReturn(Optional.of(requester));

            assertThatThrownBy(() -> meetingProfileService.openContact(meetingAuthUser, 1L))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.SELF_PROFILE_OPEN_NOT_ALLOWED.getErrorMessage());
        }

        @Test
        @DisplayName("존재하지 않는 프로필 연락처 열람 시 예외를 던진다")
        void openContact_fail_notFound() {
            MeetingProfile requester = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("requester_contact")
                    .bonusOpenCount(0)
                    .build();

            ReflectionTestUtils.setField(requester, "id", 1L);

            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-1");

            given(meetingProfileRepository.findByKakaoIdWithLock("kakao-1")).willReturn(Optional.of(requester));
            given(contactViewHistoryRepository.existsByViewerIdAndTargetId(1L, 999L)).willReturn(false);
            given(meetingProfileRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> meetingProfileService.openContact(meetingAuthUser, 999L))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.MEETING_PROFILE_NOT_FOUND.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("열람권 조회")
    class GetOpenCountTest {

        @Test
        @DisplayName("쿨다운 중일 때 열람권 0과 남은 시간을 반환한다")
        void getOpenCount_duringCooldown() {
            MeetingProfile profile = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("contact1")
                    .bonusOpenCount(1)
                    .build();

            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-1");

            given(meetingProfileRepository.findByKakaoId("kakao-1")).willReturn(Optional.of(profile));
            given(meetingOpenCountService.getRemainingCooldownSeconds("kakao-1")).willReturn(2400L);

            MeetingOpenCountResponse result = meetingProfileService.getOpenCount(meetingAuthUser);

            assertThat(result.availableOpenCount()).isEqualTo(0);
            assertThat(result.bonusOpenCount()).isEqualTo(1);
            assertThat(result.cooldownRemainingSeconds()).isEqualTo(2400L);
        }

        @Test
        @DisplayName("쿨다운이 끝났을 때 열람권 1과 남은 시간 0을 반환한다")
        void getOpenCount_cooldownFinished() {
            MeetingProfile profile = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("contact1")
                    .bonusOpenCount(0)
                    .build();

            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-1");

            given(meetingProfileRepository.findByKakaoId("kakao-1")).willReturn(Optional.of(profile));
            given(meetingOpenCountService.getRemainingCooldownSeconds("kakao-1")).willReturn(0L);

            MeetingOpenCountResponse result = meetingProfileService.getOpenCount(meetingAuthUser);

            assertThat(result.availableOpenCount()).isEqualTo(1);
            assertThat(result.bonusOpenCount()).isEqualTo(0);
            assertThat(result.cooldownRemainingSeconds()).isEqualTo(0L);
        }

        @Test
        @DisplayName("존재하지 않는 유저 열람권 조회 시 예외를 던진다")
        void getOpenCount_fail_userNotFound() {
            MeetingAuthUser meetingAuthUser = new MeetingAuthUser("kakao-999");

            given(meetingProfileRepository.findByKakaoId("kakao-999")).willReturn(Optional.empty());

            assertThatThrownBy(() -> meetingProfileService.getOpenCount(meetingAuthUser))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("삭제")
    class DeleteMeetingProfileTest {

        @Test
        @DisplayName("미팅 프로필을 정상적으로 삭제한다")
        void deleteMeetingProfile_success() {
            MeetingProfile profile = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .contact("contact1")
                    .build();

            ReflectionTestUtils.setField(profile, "id", 1L);

            given(meetingProfileRepository.findById(1L)).willReturn(Optional.of(profile));

            meetingProfileService.deleteMeetingProfile(1L);

            verify(contactViewHistoryRepository).deleteByViewerId(1L);
            verify(contactViewHistoryRepository).deleteByTargetId(1L);
            verify(meetingProfileRepository).delete(profile);
        }

        @Test
        @DisplayName("존재하지 않는 미팅 프로필 삭제 시 예외를 던진다")
        void deleteMeetingProfile_fail_notFound() {
            given(meetingProfileRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> meetingProfileService.deleteMeetingProfile(999L))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.MEETING_PROFILE_NOT_FOUND.getErrorMessage());
        }
    }
}
