package org.example.sejonglifebe.meeting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.meeting.dto.KakaoLoginRequest;
import org.example.sejonglifebe.meeting.dto.MeetingSignUpRequest;
import org.example.sejonglifebe.meeting.dto.StateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Meeting Auth", description = "미팅 서비스 인증 (카카오 로그인)")
public interface MeetingAuthControllerSwagger {

    @Operation(
            summary = "OAuth State 생성",
            description = "카카오 로그인 시 CSRF 방지를 위한 State 값을 생성합니다."
    )
    ResponseEntity<CommonResponse<StateResponse>> createOAuthState(HttpSession session);

    @Operation(
            summary = "카카오 로그인",
            description = "카카오 인가 코드를 받아 로그인 처리합니다. 신규 사용자는 signUpToken을, 기존 사용자는 accessToken을 반환하며 두 경우 모두 kakaoId를 함께 반환합니다."
    )
    ResponseEntity<CommonResponse<LoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request, HttpSession session);


    @Operation(
            summary = "미팅 회원가입",
            description = "signUpToken과 함께 추가 정보를 입력받아 회원가입을 완료하고 accessToken과 kakaoId를 반환합니다."
    )
    ResponseEntity<CommonResponse<LoginResponse>> signUp(
            @Parameter(description = "회원가입용 임시 토큰", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody MeetingSignUpRequest request,
            @RequestParam(required = false) String ref);

}
