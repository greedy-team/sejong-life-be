package org.example.sejonglifebe.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(@Value("${cloud.aws.region.static}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
