package org.example.sejonglifebe.meeting;

import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.meeting.dto.MeetingSignUpRequest;
import org.example.sejonglifebe.meeting.entity.FaceType;
import org.example.sejonglifebe.meeting.entity.Gender;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.example.sejonglifebe.meeting.service.MeetingSignUpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeetingSignUpServiceTest {

    @Mock
    private MeetingProfileRepository meetingProfileRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private MeetingSignUpService meetingSignUpService;

    private MeetingSignUpRequest buildRequest() {
        return new MeetingSignUpRequest(
                Gender.MALE,
                FaceType.DOG,
                2000,
                "축구",
                "활동적인 데이트",
                "010-1234-5678"
        );
    }

    @Nested
    @DisplayName("회원가입")
    class SignUpTest {

        @Test
        @DisplayName("ref 없이 가입하면 정상적으로 토큰을 발급한다")
        void signUp_success_withoutRef() {
            given(jwtTokenProvider.validateMeetingSignUpToken("signUpToken")).willReturn("kakao-1");
            given(meetingProfileRepository.existsByKakaoId("kakao-1")).willReturn(false);
            given(jwtTokenProvider.createMeetingToken("kakao-1")).willReturn("accessToken");

            LoginResponse response = meetingSignUpService.signUp("signUpToken", buildRequest(), null);

            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getKakaoId()).isEqualTo("kakao-1");
            verify(meetingProfileRepository, never()).findByKakaoIdWithLock(any());
        }

        @Test
        @DisplayName("유효한 ref로 가입하면 추천인의 보너스 열람권이 1 증가한다")
        void signUp_success_rewardRecommender() {
            MeetingProfile recommender = MeetingProfile.builder()
                    .kakaoId("kakao-recommender")
                    .gender(Gender.FEMALE)
                    .faceType(FaceType.CAT)
                    .birthYear(1999)
                    .hobby("영화")
                    .dateStyle("조용한 데이트")
                    .contact("contact")
                    .bonusOpenCount(0)
                    .build();

            given(jwtTokenProvider.validateMeetingSignUpToken("signUpToken")).willReturn("kakao-1");
            given(meetingProfileRepository.existsByKakaoId("kakao-1")).willReturn(false);
            given(jwtTokenProvider.createMeetingToken("kakao-1")).willReturn("accessToken");
            given(meetingProfileRepository.findByKakaoIdWithLock("kakao-recommender"))
                    .willReturn(Optional.of(recommender));

            meetingSignUpService.signUp("signUpToken", buildRequest(), "kakao-recommender");

            assertThat(recommender.getBonusOpenCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하지 않는 ref로 가입해도 정상 가입된다")
        void signUp_success_invalidRef() {
            given(jwtTokenProvider.validateMeetingSignUpToken("signUpToken")).willReturn("kakao-1");
            given(meetingProfileRepository.existsByKakaoId("kakao-1")).willReturn(false);
            given(jwtTokenProvider.createMeetingToken("kakao-1")).willReturn("accessToken");
            given(meetingProfileRepository.findByKakaoIdWithLock("kakao-unknown"))
                    .willReturn(Optional.empty());

            LoginResponse response = meetingSignUpService.signUp("signUpToken", buildRequest(), "kakao-unknown");

            assertThat(response.getAccessToken()).isEqualTo("accessToken");
        }

        @Test
        @DisplayName("자기 자신을 ref로 설정하면 보너스 열람권이 지급되지 않는다")
        void signUp_success_selfRef() {
            given(jwtTokenProvider.validateMeetingSignUpToken("signUpToken")).willReturn("kakao-1");
            given(meetingProfileRepository.existsByKakaoId("kakao-1")).willReturn(false);
            given(jwtTokenProvider.createMeetingToken("kakao-1")).willReturn("accessToken");

            meetingSignUpService.signUp("signUpToken", buildRequest(), "kakao-1");

            verify(meetingProfileRepository, never()).findByKakaoIdWithLock(any());
        }

        @Test
        @DisplayName("이미 가입된 사용자는 예외를 던진다")
        void signUp_fail_alreadyExist() {
            given(jwtTokenProvider.validateMeetingSignUpToken("signUpToken")).willReturn("kakao-1");
            given(meetingProfileRepository.existsByKakaoId("kakao-1")).willReturn(true);

            assertThatThrownBy(() -> meetingSignUpService.signUp("signUpToken", buildRequest(), null))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.ALREADY_EXIST_USER.getErrorMessage());
        }
    }
}
