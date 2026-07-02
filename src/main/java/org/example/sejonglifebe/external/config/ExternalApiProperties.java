package org.example.sejonglifebe.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external")
public record ExternalApiProperties(
        Naver naver,
        Kakao kakao,
        Google google
) {

    public record Naver(
            String clientId,
            String clientSecret
    ) {}

    public record Kakao(
            String restApiKey
    ) {}

    public record Google(
            String apiKey
    ) {}
}
