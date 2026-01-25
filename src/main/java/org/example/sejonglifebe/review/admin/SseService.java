package org.example.sejonglifebe.review.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseService {

    private final Long DEFAULT_TIMEOUT = 60 * 60 * 1000L;
    private static final Long RECONNECTION_TIMEOUT = 3000L;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String emitterId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters.put(emitterId, emitter);

        log.info("SSE 연결 생성 : emitterId={}", emitterId);

        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료 : emitterId={}", emitterId);
            emitters.remove(emitterId);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE 연결 타임아웃 : emitterId={}", emitterId);
            emitter.complete();
            emitters.remove(emitterId);
        });
        emitter.onError((e) -> {
            log.error("SSE 연결 에러 : emitterId={}", emitterId, e);
            emitter.complete();
            emitters.remove(emitterId);
        });
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결 성공")
                    .reconnectTime(RECONNECTION_TIMEOUT));
        } catch (IOException e) {
            log.error("초기 메시지 전송 실패: emitterId={}", emitterId, e);
            emitter.complete();
            emitters.remove(emitterId);
            throw new RuntimeException("SSE 연결 실패");
        }
        return emitter;
    }

    public void sendToAll(String eventName, Object data) {
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data)
                        .reconnectTime(RECONNECTION_TIMEOUT));
            } catch (IOException e) {
                log.error("SSE 메시지 전송 실패 : emitterId={}, event={}", id, eventName, e);
                emitter.complete();
                emitters.remove(id);
            }
        });
    }
}



