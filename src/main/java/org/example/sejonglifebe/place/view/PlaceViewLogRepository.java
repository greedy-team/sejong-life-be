package org.example.sejonglifebe.place.view;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceViewLogRepository extends JpaRepository<PlaceViewLog, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO place_view_log (place_id, viewer_type, viewer_key, last_viewed_at)
        VALUES (:placeId, :viewerType, :viewerKey, :now)
        ON DUPLICATE KEY UPDATE
          last_viewed_at = CASE
              WHEN last_viewed_at < :expireBefore THEN :now
              ELSE last_viewed_at
          END
        """, nativeQuery = true)
    int upsertAndTouchIfExpired(
            @Param("placeId") Long placeId,
            @Param("viewerType") String viewerType,
            @Param("viewerKey") String viewerKey,
            @Param("now") LocalDateTime now,
            @Param("expireBefore") LocalDateTime expireBefore
    );

}
