package org.example.sejonglifebe.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.meeting.dto.MeetingProfileResponse;
import org.example.sejonglifebe.meeting.service.MeetingProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meeting/profiles")
public class MeetingProfileController {

    private final MeetingProfileService meetingProfileService;

    @GetMapping
    public ResponseEntity<List<MeetingProfileResponse>> getAllMeetingProfiles() {
        return ResponseEntity.ok(meetingProfileService.getAllMeetingProfiles());
    }
}