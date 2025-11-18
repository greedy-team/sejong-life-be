package org.example.sejonglifebe.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.auth.dto.LoginRequest;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증/인가")
public class LoginController {

    private final LoginService loginService;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return CommonResponse.of(HttpStatus.OK, "로그인 성공", response);
    }

    @Operation(summary = "로그아웃", description = "클라이언트에서 저장된 토큰을 삭제해주세요.")
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout() {
        // 클라이언트 기반 로그아웃 (토큰 삭제는 클라이언트가 처리)
        // TODO: Redis 기반 토큰 블랙리스트 구현 시 여기서 토큰 무효화 처리
        return CommonResponse.of(HttpStatus.OK, "로그아웃 성공", null);
    }
}
