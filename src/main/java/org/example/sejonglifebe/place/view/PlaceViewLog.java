package org.example.sejonglifebe.place.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PlaceViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "viewer_type", nullable = false, length = 10)
    private String viewerType;

    @Column(name = "viewer_key", nullable = false, length = 128)
    private String viewerKey;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    public PlaceViewLog(Long placeId, String viewerType, String viewerKey, LocalDateTime lastViewedAt) {
        this.placeId = placeId;
        this.viewerType = viewerType;
        this.viewerKey = viewerKey;
        this.lastViewedAt = lastViewedAt;
    }

    public void updateLastViewedAt(LocalDateTime now) {
        this.lastViewedAt = now;
    }
}
