package org.example.sejonglifebe.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthInterceptor;
import org.example.sejonglifebe.auth.AuthUserArgumentResolver;
import org.example.sejonglifebe.common.logging.LogInterceptor;
import org.example.sejonglifebe.meeting.auth.MeetingAuthUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthUserArgumentResolver authUserArgumentResolver;
    private final MeetingAuthUserArgumentResolver meetingAuthUserArgumentResolver;
    private final AuthInterceptor authInterceptor;
    private final ObjectMapper objectMapper;
    private final LogInterceptor logInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserArgumentResolver);
        resolvers.add(meetingAuthUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // CorrelationIdFilter/AccessLogFilter는 actuator, swagger 등 모든 요청에 걸리지만,
        // LogInterceptor(userId/httpMethod/clientIp/userAgent)는 의도적으로 /api/** 비즈니스 요청에만 걸리도록 함.
        // actuator 헬스체크 등은 특정 사용자 컨텍스트가 없어 이 필드들이 의미가 없음.
        registry.addInterceptor(logInterceptor)
                .order(1)
                .addPathPatterns("/api/**");

        registry.addInterceptor(authInterceptor)
                .order(2)
                .addPathPatterns("/api/**");
    }
}
