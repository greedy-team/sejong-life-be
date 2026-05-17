package org.example.sejonglifebe.meeting.dto;

public record MeetingOpenCountResponse(
        int availableOpenCount,
        int bonusOpenCount,
        long cooldownRemainingSeconds
) {
}
