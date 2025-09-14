package org.example.sejonglifebe.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            String userIdAttribute = (String) request.getAttribute("userId");
            if (userIdAttribute != null && MDC.get("userId") == null)
                MDC.put("userId", userIdAttribute);

            String urlAttribute = (String) request.getAttribute("requestUrl");
            if (urlAttribute != null && MDC.get("requestUrl") == null)
                MDC.put("requestUrl", urlAttribute);

            Object errorCode = request.getAttribute("errorCode");
            if (errorCode != null)
                MDC.put("errorCode", String.valueOf(errorCode));

            int status = response.getStatus();
            MDC.put("responseCode", String.valueOf(status));

            String message = (HttpStatus.resolve(status) != null)
                    ? HttpStatus.resolve(status).getReasonPhrase() : "Processed";

            log.info(message);

            MDC.remove("responseCode");
            MDC.remove("errorCode");
        }
    }
}
