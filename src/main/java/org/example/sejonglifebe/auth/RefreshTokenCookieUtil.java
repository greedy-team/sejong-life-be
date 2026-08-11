package org.example.sejonglifebe.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieUtil {

    private static final String COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/api/auth";

    @Value("${jwt.refresh-expiration:1209600000}")
    private long refreshExpirationTime;

    @Value("${cookie.secure:true}")
    private boolean secure;

    public ResponseCookie create(String refreshToken) {
        return build(refreshToken, Duration.ofMillis(refreshExpirationTime));
    }

    public ResponseCookie expire() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
