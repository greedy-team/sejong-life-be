package org.example.sejonglifebe.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MeetingOpenCountService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration RECHARGE_COOLDOWN = Duration.ofHours(1);
    private static final String KEY_PREFIX = "meeting:recharge:";

    public boolean isRechargeable(String kakaoId) {
        return !Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + kakaoId));
    }

    public long getRemainingCooldownSeconds(String kakaoId) {
        Long ttl = redisTemplate.getExpire(KEY_PREFIX + kakaoId, TimeUnit.SECONDS);
        return (ttl != null && ttl > 0) ? ttl : 0L;
    }

    public void startCooldown(String kakaoId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + kakaoId, "1", RECHARGE_COOLDOWN);
    }

    public void clearCooldown(String kakaoId) {
        redisTemplate.delete(KEY_PREFIX + kakaoId);
    }
}
