package org.example.sejonglifebe.place.view;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.place.PlaceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {
    private final PlaceRepository placeRepository;

    //매주 월요일 00:00에 주간 조회수를 0으로 초기화
    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    @Transactional
    public void resetWeeklyViewCounts() {
        placeRepository.resetAllWeeklyViewCounts();
        log.info("주간 조회수 집계 초기화.");
    }
}
