package org.example.sejonglifebe.meeting;

import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.meeting.dto.MeetingContactResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileUpdateRequest;
import org.example.sejonglifebe.meeting.entity.FaceType;
import org.example.sejonglifebe.meeting.entity.Gender;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.example.sejonglifebe.meeting.service.MeetingProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @InjectMocks
    private MeetingProfileService meetingProfileService;

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
                    .appeal("밝음")
                    .contact("contact1")
                    .build();

            MeetingProfile profile2 = MeetingProfile.builder()
                    .kakaoId("kakao-2")
                    .gender(Gender.FEMALE)
                    .faceType(FaceType.CAT)
                    .birthYear(2001)
                    .hobby("영화")
                    .dateStyle("조용한 데이트")
                    .appeal("차분함")
                    .contact("contact2")
                    .build();

            ReflectionTestUtils.setField(profile1, "id", 1L);
            ReflectionTestUtils.setField(profile2, "id", 2L);
            ReflectionTestUtils.setField(profile1, "createdAt", LocalDateTime.of(2026, 3, 31, 10, 0));
            ReflectionTestUtils.setField(profile2, "createdAt", LocalDateTime.of(2026, 3, 31, 11, 0));

            given(meetingProfileRepository.findAll()).willReturn(List.of(profile1, profile2));

            List<MeetingProfileResponse> result = meetingProfileService.getAllMeetingProfiles();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(0).kakaoId()).isEqualTo("kakao-1");
            assertThat(result.get(0).gender()).isEqualTo("MALE");
            assertThat(result.get(0).faceType()).isEqualTo("DOG");
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
                    .appeal("밝음")
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
                    "배려심 많음",
                    "updated_contact"
            );

            given(meetingProfileRepository.findById(1L)).willReturn(Optional.of(profile));

            meetingProfileService.updateMeetingProfile(1L, request);

            assertThat(profile.getGender()).isEqualTo(Gender.FEMALE);
            assertThat(profile.getFaceType()).isEqualTo(FaceType.FOX);
            assertThat(profile.getBirthYear()).isEqualTo(1999);
            assertThat(profile.getHobby()).isEqualTo("산책");
            assertThat(profile.getDateStyle()).isEqualTo("카페 데이트");
            assertThat(profile.getAppeal()).isEqualTo("배려심 많음");
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
                    "배려심 많음",
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
        @DisplayName("연락처를 정상적으로 반환한다")
        void openContact_success() {
            MeetingProfile profile = MeetingProfile.builder()
                    .kakaoId("kakao-1")
                    .gender(Gender.MALE)
                    .faceType(FaceType.DOG)
                    .birthYear(2000)
                    .hobby("축구")
                    .dateStyle("활동적인 데이트")
                    .appeal("밝음")
                    .contact("insta_contact")
                    .build();

            ReflectionTestUtils.setField(profile, "id", 1L);

            given(meetingProfileRepository.findById(1L)).willReturn(Optional.of(profile));

            MeetingContactResponse result = meetingProfileService.openContact(1L);

            assertThat(result.contact()).isEqualTo("insta_contact");
        }

        @Test
        @DisplayName("존재하지 않는 프로필 연락처 열람 시 예외를 던진다")
        void openContact_fail_notFound() {
            given(meetingProfileRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> meetingProfileService.openContact(999L))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.MEETING_PROFILE_NOT_FOUND.getErrorMessage());
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
                    .appeal("밝음")
                    .contact("contact1")
                    .build();

            ReflectionTestUtils.setField(profile, "id", 1L);

            given(meetingProfileRepository.findById(1L)).willReturn(Optional.of(profile));

            meetingProfileService.deleteMeetingProfile(1L);

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