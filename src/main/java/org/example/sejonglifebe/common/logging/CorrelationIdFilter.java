package org.example.sejonglifebe.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = Optional.ofNullable(request.getHeader("X-Request-Id")).filter(s->!s.isBlank())
                .orElse(UUID.randomUUID().toString());
        try {
            MDC.put("requestId", requestId);
            MDC.put("uri", request.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
