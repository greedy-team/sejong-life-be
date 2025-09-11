package org.example.sejonglifebe.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    private final boolean isSecure;
    private final int defaultMaxAge;

    public CookieUtil(
            @Value("${cookie.secure}") boolean isSecure,
            @Value("${cookie.max-age}") int defaultMaxAge) {
        this.isSecure = isSecure;
        this.defaultMaxAge = defaultMaxAge;
    }

    public void addCookie(HttpServletResponse response, String name, String value) {
        addCookie(response, name, value, defaultMaxAge);
    }

    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecure);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public void expireCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst();
    }
}
