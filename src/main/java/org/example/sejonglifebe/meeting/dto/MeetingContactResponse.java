package org.example.sejonglifebe.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "연락처 열람 응답")
public record MeetingContactResponse(
        @Schema(description = "상대방 연락처")
        String contact,
        @Schema(description = "이미 열람한 프로필 여부. true이면 열람권 차감 없이 반환된 것")
        boolean alreadyViewed) {
}
