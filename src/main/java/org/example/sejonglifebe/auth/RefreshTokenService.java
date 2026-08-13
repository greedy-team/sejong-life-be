package org.example.sejonglifebe.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-expiration:1209600000}")
    private long refreshExpirationTime;

    public void save(String studentId, String refreshToken) {
        redisTemplate.opsForValue()
                .set(KEY_PREFIX + studentId, refreshToken, Duration.ofMillis(refreshExpirationTime));
    }

    public boolean matches(String studentId, String refreshToken) {
        String stored = redisTemplate.opsForValue().get(KEY_PREFIX + studentId);
        return stored != null && stored.equals(refreshToken);
    }

    public void delete(String studentId) {
        redisTemplate.delete(KEY_PREFIX + studentId);
    }
}
