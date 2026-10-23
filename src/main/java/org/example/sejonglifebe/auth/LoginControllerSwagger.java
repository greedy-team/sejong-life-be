package org.example.sejonglifebe.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.sejonglifebe.auth.dto.LoginRequest;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증/인가")
public interface LoginControllerSwagger {

    @Operation(summary = "로그인")
    ResponseEntity<CommonResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request);

    @Operation(summary = "액세스 토큰 재발급", description = "쿠키의 refresh token으로 access token을 재발급합니다.")
    ResponseEntity<CommonResponse<LoginResponse>> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken);

    @Operation(summary = "로그아웃")
    ResponseEntity<CommonResponse<Void>> logout(AuthUser authUser);

    @Operation(summary = "관리자 권한 여부 확인", description = "관리자 권한 여부를 반환합니다")
    ResponseEntity<CommonResponse<Void>> checkAdmin(AuthUser authUser);
}
