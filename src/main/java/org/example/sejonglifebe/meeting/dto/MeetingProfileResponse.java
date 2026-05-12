package org.example.sejonglifebe.meeting.dto;

import org.example.sejonglifebe.meeting.entity.MeetingProfile;

import java.time.LocalDateTime;

public record MeetingProfileResponse(
        Long id,
        String kakaoId,
        String gender,
        String faceType,
        Integer birthYear,
        String hobby,
        String dateStyle,
        String contact,
        LocalDateTime createdAt
) {
    public static MeetingProfileResponse from(MeetingProfile meetingProfile) {
        return new MeetingProfileResponse(
                meetingProfile.getId(),
                meetingProfile.getKakaoId(),
                meetingProfile.getGender().name(),
                meetingProfile.getFaceType().name(),
                meetingProfile.getBirthYear(),
                meetingProfile.getHobby(),
                meetingProfile.getDateStyle(),
                meetingProfile.getContact(),
                meetingProfile.getCreatedAt()
        );
    }
}