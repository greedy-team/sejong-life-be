package org.example.sejonglifebe.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.common.util.CookieUtil;
import org.example.sejonglifebe.user.dto.SignUpRequest;
import org.example.sejonglifebe.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<UserResponse>> signup(
            @CookieValue("signUpToken") String signUpToken,
            @Valid @RequestBody SignUpRequest request,
            HttpServletResponse httpServletResponse) {

        User newUser = userService.createUser(signUpToken, request);
        String accessToken = jwtTokenProvider.createToken(newUser);

        cookieUtil.addCookie(httpServletResponse, "accessToken", accessToken);
        cookieUtil.expireCookie(httpServletResponse, "signUpToken");

        UserResponse responseBody = UserResponse.fromEntity(newUser);
        return CommonResponse.of(HttpStatus.CREATED, "회원가입 및 로그인 성공", responseBody);
    }
}
