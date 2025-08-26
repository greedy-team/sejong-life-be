package org.example.sejonglifebe.common.jwt;

import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    public String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new SejongLifeException(ErrorCode.INVALID_AUTH_HEADER);
        }
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }
}
