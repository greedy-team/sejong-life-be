package org.example.sejonglifebe.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StateResponse(
        @Schema(description = "CSRF 방지용 State 값", example = "550e8400-e29b-41d4-a716-446655440000")
        String state
) {
}
