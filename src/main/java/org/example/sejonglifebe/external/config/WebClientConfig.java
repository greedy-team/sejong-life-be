package org.example.sejonglifebe.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient naverWebClient() {
        return WebClient.builder()
                .baseUrl("https://openapi.naver.com")
                .build();
    }

    @Bean
    public WebClient kakaoWebClient() {
        return WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .build();
    }

    @Bean
    public WebClient googleWebClient() {
        return WebClient.builder()
                .baseUrl("https://places.googleapis.com")
                .build();
    }
}
