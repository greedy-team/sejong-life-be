package org.example.sejonglifebe.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.user.dto.SignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<String>> signup(
            @RequestHeader("Authorization") String signUpToken,
            @Valid @RequestBody SignUpRequest request) {

        String pureToken = resolveToken(signUpToken);
        PortalStudentInfo portalInfoFromToken = jwtTokenProvider.validateAndGetPortalInfo(pureToken);

        if (!portalInfoFromToken.getStudentId().equals(request.getStudentId()) ||
                !portalInfoFromToken.getName().equals(request.getName())) {
            throw new SecurityException("토큰의 정보와 요청된 사용자 정보가 일치하지 않습니다.");
        }

        String accessToken = userService.createUser(request);
        return CommonResponse.of(HttpStatus.CREATED, "회원가입 및 로그인 성공", accessToken);
    }

    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
    }
}
