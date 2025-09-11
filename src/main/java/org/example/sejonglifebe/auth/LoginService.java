package org.example.sejonglifebe.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.auth.dto.LoginRequest;
import org.example.sejonglifebe.auth.dto.LoginResult;
import org.example.sejonglifebe.auth.dto.PortalStudentInfo;
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
    private final PortalHtmlParser portalHtmlParser;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResult login(LoginRequest request) {
        try {
            String html = fetchPortal(request);
            PortalStudentInfo studentInfo = portalHtmlParser.parseStudentInfo(html);

            // 가입 여부 확인
            Optional<User> userOptional = userService.findUserByStudentId(studentInfo.studentId());

            if (userOptional.isPresent()) {
                // 기존 회원: accessToken 담은 LoginResult 반환
                User user = userOptional.get();
                String accessToken = jwtTokenProvider.createToken(user);
                return LoginResult.forExistingUser(accessToken);
            } else {
                // 신규 회원: signUpToken과 기본 정보 담은 LoginResult 반환
                String signUpToken = jwtTokenProvider.createSignUpToken(studentInfo);
                return LoginResult.forNewUser(signUpToken, studentInfo);
            }

        } catch (SejongLifeException e) {
            log.error("세종포털 인증 중 예측하지 못한 오류 발생", e);
            throw new SejongLifeException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    // 인증 로직
    private String fetchPortal(LoginRequest request) {
        portalClient.login(request.getSejongPortalId(), request.getSejongPortalPw());
        return portalClient.fetchHtml();
    }
}
