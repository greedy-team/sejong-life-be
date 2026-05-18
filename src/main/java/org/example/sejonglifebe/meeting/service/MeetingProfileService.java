package org.example.sejonglifebe.meeting.service;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.meeting.dto.MeetingContactResponse;
import org.example.sejonglifebe.meeting.dto.MeetingAuthUser;
import org.example.sejonglifebe.meeting.dto.MeetingOpenCountResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileUpdateRequest;
import org.example.sejonglifebe.meeting.entity.ContactViewHistory;
import org.example.sejonglifebe.meeting.entity.Gender;
import org.example.sejonglifebe.meeting.entity.MeetingProfile;
import org.example.sejonglifebe.meeting.repository.ContactViewHistoryRepository;
import org.example.sejonglifebe.meeting.repository.MeetingProfileRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingProfileService {

    private final MeetingProfileRepository meetingProfileRepository;
    private final ContactViewHistoryRepository contactViewHistoryRepository;
    private final MeetingOpenCountService meetingOpenCountService;
    private final ApplicationEventPublisher eventPublisher;

    public MeetingOpenCountResponse getOpenCount(MeetingAuthUser meetingAuthUser) {
        MeetingProfile profile = meetingProfileRepository.findByKakaoId(meetingAuthUser.kakaoId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));

        long remaining = meetingOpenCountService.getRemainingCooldownSeconds(meetingAuthUser.kakaoId());
        int availableCount = remaining == 0 ? 1 : 0;

        return new MeetingOpenCountResponse(availableCount, profile.getBonusOpenCount(), remaining);
    }

    public List<MeetingProfileResponse> getAllMeetingProfiles(MeetingAuthUser meetingAuthUser) {
        MeetingProfile meetingProfile = meetingProfileRepository.findByKakaoId(meetingAuthUser.kakaoId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));

        Gender oppositeGender = meetingProfile.getGender() == Gender.MALE ? Gender.FEMALE : Gender.MALE;

        return meetingProfileRepository.findByGender(oppositeGender).stream()
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
                request.contact()
        );
    }

    @Transactional
    public MeetingContactResponse openContact(MeetingAuthUser meetingAuthUser, Long profileId) {
        MeetingProfile requester = meetingProfileRepository.findByKakaoIdWithLock(meetingAuthUser.kakaoId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));

        if (requester.getId().equals(profileId)) {
            throw new SejongLifeException(ErrorCode.SELF_PROFILE_OPEN_NOT_ALLOWED);
        }

        boolean alreadyViewed = contactViewHistoryRepository
                .existsByViewerIdAndTargetId(requester.getId(), profileId);

        MeetingProfile target = meetingProfileRepository.findById(profileId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.MEETING_PROFILE_NOT_FOUND));

        if (!alreadyViewed) {
            if (requester.hasBonusOpenCount()) {
                requester.decreaseBonusOpenCount();
            } else if (meetingOpenCountService.isRechargeable(meetingAuthUser.kakaoId())) {
                eventPublisher.publishEvent(new CooldownStartEvent(meetingAuthUser.kakaoId()));
            } else {
                throw new SejongLifeException(ErrorCode.INSUFFICIENT_OPEN_COUNT);
            }
            contactViewHistoryRepository.save(ContactViewHistory.builder()
                    .viewer(requester)
                    .target(target)
                    .build());
        }

        return new MeetingContactResponse(target.getContact(), alreadyViewed);
    }

    @Transactional
    public void deleteMeetingProfile(Long id) {
        MeetingProfile profile = meetingProfileRepository.findById(id)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.MEETING_PROFILE_NOT_FOUND));

        contactViewHistoryRepository.deleteByViewerId(id);
        contactViewHistoryRepository.deleteByTargetId(id);
        meetingProfileRepository.delete(profile);
    }
}
