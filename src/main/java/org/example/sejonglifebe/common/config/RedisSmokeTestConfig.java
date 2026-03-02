package org.example.sejonglifebe.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisSmokeTestConfig {

    private final StringRedisTemplate redisTemplate;

    @Bean
    public ApplicationRunner redisPingRunner() {
        return args -> {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            System.out.println("[REDIS PING] " + pong);

            // set/get까지 확인하고 싶으면:
            redisTemplate.opsForValue().set("hello", "world");
            System.out.println("[REDIS GET] " + redisTemplate.opsForValue().get("hello"));
        };
    }
}
