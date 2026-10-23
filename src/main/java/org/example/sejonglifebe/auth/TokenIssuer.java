package org.example.sejonglifebe.auth;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.user.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenIssuer {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse issue(User user) {
        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);
        refreshTokenService.save(user.getStudentId(), refreshToken);

        return LoginResponse.loginSuccessWithRefreshToken(accessToken, refreshToken);
    }
}
