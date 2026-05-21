package org.example.sejonglifebe.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.meeting.dto.MeetingContactResponse;
import org.example.sejonglifebe.meeting.dto.MeetingAuthUser;
import org.example.sejonglifebe.meeting.dto.MeetingOpenCountResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileCountResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileUpdateRequest;
import org.example.sejonglifebe.meeting.service.MeetingProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meeting/profiles")
public class MeetingProfileController implements MeetingProfileControllerSwagger {

    private final MeetingProfileService meetingProfileService;

    @GetMapping("/count")
    public ResponseEntity<MeetingProfileCountResponse> getProfileCount() {
        return ResponseEntity.ok(meetingProfileService.getProfileCount());
    }

    @GetMapping
    public ResponseEntity<List<MeetingProfileResponse>> getAllMeetingProfiles(MeetingAuthUser meetingAuthUser) {
        return ResponseEntity.ok(meetingProfileService.getAllMeetingProfiles(meetingAuthUser));
    }

    @GetMapping("/open-count")
    public ResponseEntity<MeetingOpenCountResponse> getOpenCount(MeetingAuthUser meetingAuthUser) {
        return ResponseEntity.ok(meetingProfileService.getOpenCount(meetingAuthUser));
    }

    @PostMapping("/{profileId}/open")
    public ResponseEntity<MeetingContactResponse> openContact(
            MeetingAuthUser meetingAuthUser,
            @PathVariable Long profileId
    ) {
        return ResponseEntity.ok(meetingProfileService.openContact(meetingAuthUser, profileId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeetingProfile(@PathVariable Long id) {
        meetingProfileService.deleteMeetingProfile(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("")
    public ResponseEntity<Void> deleteMyMeetingProfile(MeetingAuthUser meetingAuthUser) {
        meetingProfileService.deleteMyMeetingProfile(meetingAuthUser);
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
