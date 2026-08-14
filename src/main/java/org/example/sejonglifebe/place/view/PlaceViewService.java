package org.example.sejonglifebe.place.view;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PlaceViewService {
    private final StringRedisTemplate redisTemplate;
    private final Duration viewTimeToLive;

    public PlaceViewService(StringRedisTemplate redisTemplate,
                            @Value("${place.view.dedup-ttl:30s}") Duration viewTimeToLive) {
        this.redisTemplate = redisTemplate;
        this.viewTimeToLive = viewTimeToLive;
    }

    public boolean recordFirstView(Long placeId, Viewer viewer) {
        String key = buildKey(placeId, viewer);

        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", viewTimeToLive);

        return Boolean.TRUE.equals(ok);
    }

    private String buildKey(Long placeId, Viewer viewer) {
        return "pv:" + placeId + ":" + viewer.type() + ":" + viewer.key();
    }
}
