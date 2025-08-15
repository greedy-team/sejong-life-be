package org.example.sejonglifebe.common.dto;

public record ErrorResponse(
        String errorCode,
        String message,
        Object data
) {
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message, null);
    }
}
