package org.example.sejonglifebe.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.LoginRequired;
import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.auth.RefreshTokenCookieUtil;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenExtractor;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.user.dto.MyPageResponse;
import org.example.sejonglifebe.user.dto.SignUpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserControllerSwagger{

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenExtractor jwtTokenExtractor;
    private final RefreshTokenCookieUtil refreshTokenCookieUtil;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<LoginResponse>> signup(
            @RequestHeader("Authorization") String signUpToken,
            @Valid @RequestBody SignUpRequest request) {

        String pureToken = jwtTokenExtractor.extractToken(signUpToken);
        PortalStudentInfo portalInfoFromToken = jwtTokenProvider.validateAndGetPortalInfo(pureToken);

        if (!portalInfoFromToken.getStudentId().equals(request.getStudentId()) ||
                !portalInfoFromToken.getName().equals(request.getName())) {
            throw new SejongLifeException(ErrorCode.INVALID_TOKEN);
        }

        LoginResponse response = userService.createUser(request, portalInfoFromToken);
        ResponseCookie refreshTokenCookie = refreshTokenCookieUtil.create(response.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new CommonResponse<>("회원가입 및 로그인 성공", response));
    }

    @LoginRequired
    @DeleteMapping
    public ResponseEntity<CommonResponse<Void>> deleteUser(AuthUser authUser) {
        userService.deleteUser(authUser);
        return CommonResponse.of(HttpStatus.OK, "회원 탈퇴 성공", null);
    }

    @LoginRequired
    @GetMapping
    public ResponseEntity<CommonResponse<MyPageResponse>> getMyPageInfo(AuthUser authUser) {
        MyPageResponse userInfo = userService.getMyPageInfo(authUser);
        return CommonResponse.of(HttpStatus.OK, "마이페이지 정보 조회 성공", userInfo);
    }
}
