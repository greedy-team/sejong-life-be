package org.example.sejonglifebe.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenExtractor;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.user.dto.SignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenExtractor jwtTokenExtractor;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<String>> signup(
            @RequestHeader("Authorization") String signUpToken,
            @Valid @RequestBody SignUpRequest request) {

        String pureToken = jwtTokenExtractor.extractToken(signUpToken);
        PortalStudentInfo portalInfoFromToken = jwtTokenProvider.validateAndGetPortalInfo(pureToken);

        if (!portalInfoFromToken.getStudentId().equals(request.getStudentId()) ||
                !portalInfoFromToken.getName().equals(request.getName())) {
            throw new SejongLifeException(ErrorCode.INVALID_TOKEN);
        }

        String accessToken = userService.createUser(request);
        return CommonResponse.of(HttpStatus.CREATED, "회원가입 및 로그인 성공", accessToken);
    }
}
