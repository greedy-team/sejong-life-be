package org.example.sejonglifebe.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.meeting.dto.MeetingAuthUser;
import org.example.sejonglifebe.meeting.dto.MeetingProfileResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileUpdateRequest;
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
    public ResponseEntity<List<MeetingProfileResponse>> getAllMeetingProfiles(MeetingAuthUser meetingAuthUser) {
        return ResponseEntity.ok(meetingProfileService.getAllMeetingProfiles(meetingAuthUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeetingProfile(@PathVariable Long id) {
        meetingProfileService.deleteMeetingProfile(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMeetingProfile(
            @PathVariable Long id,
            @RequestBody MeetingProfileUpdateRequest request
    ) {
        meetingProfileService.updateMeetingProfile(id, request);
        return ResponseEntity.noContent().build();
    }
}