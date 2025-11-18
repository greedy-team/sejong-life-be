package org.example.sejonglifebe.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.auth.dto.LoginRequest;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final PortalClient portalClient;
    private final PortalHtmlParser htmlParser;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        String html = fetchPortal(request);
        PortalStudentInfo studentInfo = htmlParser.parseStudentInfo(html);

        // 가입 여부 확인
        Optional<User> userOptional = userService.findUserByStudentId(studentInfo.getStudentId());

        if (userOptional.isPresent()) {
            // 기존 회원: JWT 발급 후 로그인 성공 응답
            User user = userOptional.get();
            String accessToken = jwtTokenProvider.createToken(user);
            return LoginResponse.loginSuccess(accessToken);
        } else {
            // 신규 회원: 회원가입 토큰 발급 후 가입 필요 응답
            String signUpToken = jwtTokenProvider.createSignUpToken(studentInfo);
            return LoginResponse.signUpRequired(signUpToken, studentInfo);
        }
    }

    // 인증 로직
    private String fetchPortal(LoginRequest request) {
        portalClient.login(request.getSejongPortalId(), request.getSejongPortalPw());
        return portalClient.fetchHtml();
    }
}
