package org.example.sejonglifebe.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.auth.dto.LoginRequest;
import org.example.sejonglifebe.auth.dto.LoginResult;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.common.util.CookieUtil;
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
    private final CookieUtil cookieUtil;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(
            HttpServletResponse httpServletResponse,
            @Valid @RequestBody LoginRequest request) {

        LoginResult result = loginService.login(request);
        LoginResponse responseBody;

        if (result.isNewUser()) {
            // 신규 유저
            cookieUtil.addCookie(httpServletResponse, "signUpToken", result.token());
            responseBody = LoginResponse.signUpRequired(result.studentInfo());
        } else {
            // 기존 유저
            cookieUtil.addCookie(httpServletResponse, "accessToken", result.token());
            responseBody = LoginResponse.loginSuccess();
        }

        return CommonResponse.of(HttpStatus.OK, "로그인 성공", responseBody);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(HttpServletResponse response) {
        cookieUtil.expireCookie(response, "accessToken");
        cookieUtil.expireCookie(response, "signUpToken");
        return CommonResponse.of(HttpStatus.OK, "로그아웃 성공", null);
    }
}
