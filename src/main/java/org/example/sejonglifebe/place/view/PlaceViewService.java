package org.example.sejonglifebe.place.view;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.place.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PlaceViewService {

    private final PlaceViewLogRepository placeViewLogRepository;
    private final PlaceRepository placeRepository;
    private static final Duration VIEW_TIME_TO_LIVE = Duration.ofHours(6);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseViewCount(Long placeId, AuthUser authUser, HttpServletRequest request) {
        Viewer viewer = identifyViewer(authUser, request);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireBefore = now.minus(VIEW_TIME_TO_LIVE);

        int affected = placeViewLogRepository.upsertAndTouchIfExpired(
                placeId, viewer.type(), viewer.key(), now, expireBefore
        );

        if (affected != 0) {
            placeRepository.increaseViewCount(placeId);
        }
    }

    private Viewer identifyViewer(AuthUser authUser, HttpServletRequest request) {
        if (authUser != null && StringUtils.hasText(authUser.studentId())) {
            return Viewer.user(authUser.studentId());
        }
        return Viewer.ipua(ViewerKeyGenerator.ipUaHash(request));
    }
}
