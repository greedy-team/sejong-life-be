package org.example.sejonglifebe.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.jwt.JwtTokenExtractor;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        LoginRequired loginRequired = method.getMethodAnnotation(LoginRequired.class);
        if (loginRequired == null) {
            return true;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = jwtTokenExtractor.extractToken(authHeader);
        jwtTokenProvider.validateAndGetAuthUser(token);
        return true;
    }
}
