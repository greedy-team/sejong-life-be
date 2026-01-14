package org.example.sejonglifebe.place.view;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceViewLogRepository extends JpaRepository<PlaceViewLog, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT v FROM PlaceViewLog v
                WHERE v.placeId = :placeId AND v.viewerType = :viewerType AND v.viewerKey = :viewerKey
           """)
    Optional<PlaceViewLog> findForUpdate(@Param("placeId") Long placeId,
                                         @Param("viewerType") String viewerType,
                                         @Param("viewerKey") String viewerKey);
}
