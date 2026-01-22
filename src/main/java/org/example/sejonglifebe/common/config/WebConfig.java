package org.example.sejonglifebe.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthInterceptor;
import org.example.sejonglifebe.auth.AuthUserArgumentResolver;
import org.example.sejonglifebe.common.logging.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthUserArgumentResolver authUserArgumentResolver;
    private final AuthInterceptor authInterceptor;
    private final ObjectMapper objectMapper;
    private final LogInterceptor logInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .order(1)
                .addPathPatterns("/api/**");

        registry.addInterceptor(authInterceptor)
                .order(2)
                .addPathPatterns("/api/**");
    }
}
