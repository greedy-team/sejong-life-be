package org.example.sejonglifebe.meeting.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contact_view_histories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"viewer_profile_id", "target_profile_id"}))
public class ContactViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_profile_id", nullable = false)
    private MeetingProfile viewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_profile_id", nullable = false)
    private MeetingProfile target;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    @Builder
    public ContactViewHistory(MeetingProfile viewer, MeetingProfile target) {
        this.viewer = viewer;
        this.target = target;
    }
}
