package org.example.sejonglifebe.place.view;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceViewLogRepository extends JpaRepository<PlaceViewLog, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE PlaceViewLog v
        SET v.lastViewedAt = :now
        WHERE v.placeId = :placeId
          AND v.viewerType = :viewerType
          AND v.viewerKey = :viewerKey
          AND v.lastViewedAt <= :expireBefore
    """)
    int updateIfExpired(@Param("placeId") Long placeId,
                        @Param("viewerType") String viewerType,
                        @Param("viewerKey") String viewerKey,
                        @Param("now") LocalDateTime now,
                        @Param("expireBefore") LocalDateTime expireBefore);

    boolean existsByPlaceIdAndViewerTypeAndViewerKey(Long placeId, String viewerType, String viewerKey);

}
