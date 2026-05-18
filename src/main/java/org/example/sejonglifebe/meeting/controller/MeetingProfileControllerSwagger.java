package org.example.sejonglifebe.meeting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.sejonglifebe.meeting.dto.MeetingAuthUser;
import org.example.sejonglifebe.meeting.dto.MeetingContactResponse;
import org.example.sejonglifebe.meeting.dto.MeetingOpenCountResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileResponse;
import org.example.sejonglifebe.meeting.dto.MeetingProfileUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Meeting Profile", description = "미팅 프로필 관리")
public interface MeetingProfileControllerSwagger {

    @Operation(
            summary = "미팅 프로필 전체 조회",
            description = "로그인한 사용자의 반대 성별 미팅 프로필 목록을 조회합니다."
    )
    ResponseEntity<List<MeetingProfileResponse>> getAllMeetingProfiles(MeetingAuthUser meetingAuthUser);

    @Operation(
            summary = "열람권 현황 조회",
            description = "보너스 열람권 수량과 쿨다운 남은 시간(초)을 반환합니다. 쿨다운이 끝났으면 사용 가능 열람권을 1로 반환합니다."
    )
    ResponseEntity<MeetingOpenCountResponse> getOpenCount(MeetingAuthUser meetingAuthUser);

    @Operation(
            summary = "연락처 열람",
            description = "열람권을 사용해 상대방의 연락처를 조회합니다. 보너스 열람권을 먼저 차감하며, 보너스가 없고 쿨다운이 끝났으면 쿨다운을 시작하고 연락처를 반환합니다."
    )
    ResponseEntity<MeetingContactResponse> openContact(
            MeetingAuthUser meetingAuthUser,
            @Parameter(description = "열람할 프로필 ID", required = true)
            @PathVariable Long profileId
    );

    @Operation(
            summary = "미팅 프로필 수정",
            description = "미팅 프로필 정보를 수정합니다."
    )
    ResponseEntity<Void> updateMeetingProfile(
            @Parameter(description = "프로필 ID", required = true)
            @PathVariable Long id,
            @RequestBody MeetingProfileUpdateRequest request
    );

    @Operation(
            summary = "미팅 프로필 삭제",
            description = "미팅 프로필을 삭제합니다."
    )
    ResponseEntity<Void> deleteMeetingProfile(
            @Parameter(description = "프로필 ID", required = true)
            @PathVariable Long id
    );
}
