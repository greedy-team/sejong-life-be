package org.example.sejonglifebe.place.view;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceViewService {
    private final StringRedisTemplate redisTemplate;
    private static final Duration VIEW_TIME_TO_LIVE = Duration.ofHours(6);

    public boolean recordFirstView(Long placeId, Viewer viewer) {
        String key = buildKey(placeId, viewer);

        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", VIEW_TIME_TO_LIVE);

        return Boolean.TRUE.equals(ok);
    }

    private String buildKey(Long placeId, Viewer viewer) {
        return "pv:" + placeId + ":" + viewer.type() + ":" + viewer.key();
    }
}
