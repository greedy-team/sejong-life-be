package org.example.sejonglifebe.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = Optional.ofNullable(request.getHeader("X-USER-ID"))
                .filter(s -> !s.isBlank())
                .orElse("GUEST");

        MDC.put("userId", userId);
        MDC.put("requestUrl", request.getRequestURI());
        MDC.put("httpMethod", request.getMethod());
        MDC.put("clientIp", request.getRemoteAddr());
        MDC.put("userAgent", request.getHeader("User-Agent"));

        request.setAttribute("userId", userId);
        request.setAttribute("requestUrl", request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        MDC.remove("userId");
        MDC.remove("requestUrl");
        MDC.remove("httpMethod");
        MDC.remove("clientIp");
        MDC.remove("userAgent");
    }
}
