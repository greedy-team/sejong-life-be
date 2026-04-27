package org.example.sejonglifebe.meeting.dto;

import org.example.sejonglifebe.meeting.entity.FaceType;
import org.example.sejonglifebe.meeting.entity.Gender;

public record MeetingProfileUpdateRequest(
        Gender gender,
        FaceType faceType,
        Integer birthYear,
        String hobby,
        String dateStyle,
        String appeal,
        String contact
) {}