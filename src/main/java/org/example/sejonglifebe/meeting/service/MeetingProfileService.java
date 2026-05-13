package org.example.sejonglifebe.meeting.service;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.meeting.dto.MeetingContactResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileUpdateRequest;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingProfileService {

    private final MeetingProfileRepository meetingProfileRepository;

    public List<MeetingProfileResponse> getAllMeetingProfiles() {
        return meetingProfileRepository.findAll().stream()
                .map(MeetingProfileResponse::from)
                .toList();
    }

    @Transactional
    public void updateMeetingProfile(Long id, MeetingProfileUpdateRequest request) {
        MeetingProfile profile = meetingProfileRepository.findById(id)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.MEETING_PROFILE_NOT_FOUND));

        profile.update(
                request.gender(),
                request.faceType(),
                request.birthYear(),
                request.hobby(),
                request.dateStyle(),
                request.appeal(),
                request.contact()
        );
    }

    public MeetingContactResponse openContact(Long id) {
        MeetingProfile profile = meetingProfileRepository.findById(id)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.MEETING_PROFILE_NOT_FOUND));
        return new MeetingContactResponse(profile.getContact());
    }

    @Transactional
    public void deleteMeetingProfile(Long id) {
        MeetingProfile profile = meetingProfileRepository.findById(id)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.MEETING_PROFILE_NOT_FOUND));

        meetingProfileRepository.delete(profile);
    }
}