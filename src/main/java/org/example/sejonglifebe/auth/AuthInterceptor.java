package org.example.sejonglifebe.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.dto.LoginUser;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.common.util.CookieUtil;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String AUTH_USER_ATTRIBUTE = "authUser";

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        LoginRequired loginRequired = method.getMethodAnnotation(LoginRequired.class);
        if (loginRequired == null) {
            return true;
        }

        Optional<Cookie> tokenCookie = cookieUtil.getCookie(request, "accessToken");

        if (tokenCookie.isEmpty()) {
            if (loginRequired.required()) {
                throw new SejongLifeException(ErrorCode.LOGIN_REQUIRED);
            }
            return true;
        }

        String token = tokenCookie.get().getValue();
        try {
            LoginUser loginUser = jwtTokenProvider.validateAndGetAuthUser(token);
            request.setAttribute(AUTH_USER_ATTRIBUTE, loginUser);
            return true;
        } catch (Exception e) {
                if (loginRequired.required()) {
                    throw new SejongLifeException(ErrorCode.INVALID_TOKEN, e);
                }
                return true;
            }
    }
}
