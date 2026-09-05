package org.example.sejonglifebe.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("X-USER-ID");
        if (userId == null || userId.isBlank()) {
            userId = "GUEST";
        }

        MDC.put("userId", userId);
        MDC.put("httpMethod", request.getMethod());
        MDC.put("clientIp", request.getRemoteAddr());
        MDC.put("userAgent", request.getHeader("User-Agent"));

        return true;
    }

    // MDC 정리는 CorrelationIdFilter의 최종 MDC.clear()가 담당.
    // 여기서 먼저 지우면 AccessLogFilter가 access log를 남기는 시점엔
    // 이미 userId 등이 비어있는 상태가 되어버림.
}
