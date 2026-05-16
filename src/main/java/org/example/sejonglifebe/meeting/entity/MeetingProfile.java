package org.example.sejonglifebe.meeting.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "meeting_profiles")
public class MeetingProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_profile_id")
    private Long id;

    @Column(name = "kakao_id", unique = true, nullable = false)
    private String kakaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "face_type", nullable = false, length = 20)
    private FaceType faceType;

    @Column(name = "birth_year", nullable = false)
    private Integer birthYear;

    @Column(name = "hobby", nullable = false, length = 50)
    private String hobby;

    @Column(name = "date_style", nullable = false, length = 100)
    private String dateStyle;

    @Column(name = "contact", nullable = false, length = 100)
    private String contact;

    @Column(name = "available_open_count", nullable = false)
    private int availableOpenCount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void decreaseOpenCount() {
        this.availableOpenCount--;
    }

    public void update(
            Gender gender,
            FaceType faceType,
            Integer birthYear,
            String hobby,
            String dateStyle,
            String contact
    ) {
        this.gender = gender;
        this.faceType = faceType;
        this.birthYear = birthYear;
        this.hobby = hobby;
        this.dateStyle = dateStyle;
        this.contact = contact;
    }
}
