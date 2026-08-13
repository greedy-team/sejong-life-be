package org.example.sejonglifebe.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.auth.dto.LoginRequest;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.user.Role;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController implements LoginControllerSwagger {

    private final LoginService loginService;
    private final RefreshTokenCookieUtil refreshTokenCookieUtil;

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.OK);
        if (response.getRefreshToken() != null) {
            ResponseCookie refreshTokenCookie = refreshTokenCookieUtil.create(response.getRefreshToken());
            responseBuilder.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        }

        return responseBuilder.body(new CommonResponse<>("로그인 성공", response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<LoginResponse>> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new SejongLifeException(ErrorCode.INVALID_TOKEN);
        }

        LoginResponse response = loginService.reissue(refreshToken);
        ResponseCookie refreshTokenCookie = refreshTokenCookieUtil.create(response.getRefreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new CommonResponse<>("토큰 재발급 성공", response));
    }

    @LoginRequired
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(AuthUser authUser) {
        loginService.logout(authUser.studentId());
        ResponseCookie expiredCookie = refreshTokenCookieUtil.expire();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(new CommonResponse<>("로그아웃 성공", null));
    }

    @LoginRequired(role = Role.ADMIN)
    @GetMapping("/admin")
    public ResponseEntity<CommonResponse<Void>> checkAdmin(AuthUser authUser) {
        return CommonResponse.of(HttpStatus.OK, "관리자 권한 확인 성공", null);
    }
}
