package org.example.sejonglifebe.meeting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.example.sejonglifebe.meeting.dto.KakaoTokenResponse;
import org.example.sejonglifebe.meeting.dto.KakaoUserInfo;
import org.example.sejonglifebe.meeting.oauth.KakaoOAuthClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final MeetingProfileRepository meetingProfileRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse loginWithKakao(String code) {
        // 1. 인가 코드로 카카오 액세스 토큰 발급
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.getAccessToken(code);

        // 2. 액세스 토큰으로 카카오 사용자 정보 조회
        KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.getUserInfo(tokenResponse.getAccess_token());

        // 3. 카카오 ID 생성
        String kakaoId = "kakao_" + kakaoUserInfo.getId();

        // 4. MeetingProfile 확인
        Optional<MeetingProfile> profileOptional = meetingProfileRepository.findByKakaoId(kakaoId);

        if (profileOptional.isPresent()) {
            // 이미 회원가입 완료: 미팅 JWT 발급
            String accessToken = jwtTokenProvider.createMeetingToken(kakaoId);
            return LoginResponse.loginSuccess(accessToken, kakaoId);

        } else {
            // 신규 사용자: 회원가입용 임시 토큰 발급
            String signUpToken = jwtTokenProvider.createMeetingSignUpToken(kakaoId);
            return LoginResponse.signUpRequired(signUpToken, kakaoId);
        }
    }
}
