package org.example.sejonglifebe.meeting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.meeting.dto.MeetingSignUpRequest;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.example.sejonglifebe.meeting.repository.MeetingWithdrawalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingSignUpService {

    private final MeetingProfileRepository meetingProfileRepository;
    private final MeetingWithdrawalRepository meetingWithdrawalRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse signUp(String signUpToken, MeetingSignUpRequest request, String ref) {
        String kakaoId = jwtTokenProvider.validateMeetingSignUpToken(signUpToken);

        if (meetingProfileRepository.existsByKakaoId(kakaoId)) {
            throw new SejongLifeException(ErrorCode.ALREADY_EXIST_USER);
        }

        MeetingProfile meetingProfile = MeetingProfile.builder()
                .kakaoId(kakaoId)
                .gender(request.gender())
                .faceType(request.faceType())
                .birthYear(request.birthYear())
                .hobby(request.hobby())
                .dateStyle(request.dateStyle())
                .contact(request.contact())
                .build();

        meetingProfileRepository.save(meetingProfile);

        rewardRecommender(kakaoId, ref);

        String accessToken = jwtTokenProvider.createMeetingToken(kakaoId);
        return LoginResponse.loginSuccess(accessToken, kakaoId);
    }

    private void rewardRecommender(String newUserKakaoId, String recommendId) {
        if (recommendId == null || recommendId.isBlank()) {
            return;
        }
        if (recommendId.equals(newUserKakaoId)) {
            return;
        }
        if (meetingWithdrawalRepository.existsByKakaoId(newUserKakaoId)) {
            return;
        }
        meetingProfileRepository.findByKakaoIdWithLock(recommendId)
                .ifPresent(MeetingProfile::increaseBonusOpenCount);
    }
}
