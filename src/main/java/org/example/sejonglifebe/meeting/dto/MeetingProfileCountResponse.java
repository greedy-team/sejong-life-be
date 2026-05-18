package org.example.sejonglifebe.meeting.dto;

public record MeetingProfileCountResponse(
        long total,
        long male,
        long female
) {
}
