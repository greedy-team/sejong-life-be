package org.example.sejonglifebe.meeting.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenExtractor;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.meeting.dto.KakaoLoginRequest;
import org.example.sejonglifebe.meeting.dto.MeetingSignUpRequest;
import org.example.sejonglifebe.meeting.dto.StateResponse;
import org.example.sejonglifebe.meeting.service.KakaoLoginService;
import org.example.sejonglifebe.meeting.service.MeetingSignUpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Parameter;

import java.util.UUID;

@RestController
@RequestMapping("/api/meeting/auth")
@RequiredArgsConstructor

public class MeetingAuthController implements MeetingAuthControllerSwagger{

    private final KakaoLoginService kakaoLoginService;
    private final MeetingSignUpService meetingSignUpService;
    private final JwtTokenExtractor jwtTokenExtractor;

    @GetMapping("/kakao/state")
    public ResponseEntity<CommonResponse<StateResponse>> createOAuthState(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth_state", state);
        return CommonResponse.of(HttpStatus.OK, "State 생성 완료", new StateResponse(state));
    }

    @PostMapping("/kakao/login")
    public ResponseEntity<CommonResponse<LoginResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request,
            HttpSession session) {

        String savedState = (String) session.getAttribute("oauth_state");
        if (savedState == null || !savedState.equals(request.getState())) {
            throw new SejongLifeException(ErrorCode.INVALID_OAUTH_STATE);
        }
        session.removeAttribute("oauth_state");

        LoginResponse response = kakaoLoginService.loginWithKakao(request.getCode());
        return CommonResponse.of(HttpStatus.OK, "카카오 로그인 성공", response);
    }

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<LoginResponse>> signUp(
            @Parameter(description = "회원가입용 임시 토큰", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody MeetingSignUpRequest request) {

        String signUpToken = jwtTokenExtractor.extractToken(authHeader);

        LoginResponse response = meetingSignUpService.signUp(signUpToken, request);
        return CommonResponse.of(HttpStatus.CREATED, "회원가입 완료", response);
    }
}
