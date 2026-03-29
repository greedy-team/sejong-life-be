package org.example.sejonglifebe.meeting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.example.sejonglifebe.meeting.dto.MeetingSignUpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingSignUpService {

    private final MeetingProfileRepository meetingProfileRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse signUp(String signUpToken, MeetingSignUpRequest request) {
        // 1. signUpToken 검증 및 kakaoId 추출
        String kakaoId = jwtTokenProvider.validateMeetingSignUpToken(signUpToken);

        // 2. 이미 가입된 사용자인지 확인
        if (meetingProfileRepository.existsByKakaoId(kakaoId)) {
            throw new SejongLifeException(ErrorCode.ALREADY_EXIST_USER);
        }

        // 3. MeetingProfile 생성 및 저장
        MeetingProfile meetingProfile = MeetingProfile.builder()
                .kakaoId(kakaoId)
                .gender(request.gender())
                .faceType(request.faceType())
                .birthYear(request.birthYear())
                .hobby(request.hobby())
                .dateStyle(request.dateStyle())
                .appeal(request.appeal())
                .contact(request.contact())
                .build();

        meetingProfileRepository.save(meetingProfile);

        // 4. 최종 JWT 토큰 발급 (미팅 전용)
        String accessToken = jwtTokenProvider.createMeetingToken(kakaoId);
        return LoginResponse.loginSuccess(accessToken);
    }
}
