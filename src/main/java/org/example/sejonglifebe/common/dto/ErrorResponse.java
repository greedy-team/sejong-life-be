package org.example.sejonglifebe.common.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ErrorResponse<T>(
        String errorCode,
        String message,
        T data
) {

    public static <T> ResponseEntity<ErrorResponse<T>> of(HttpStatus httpStatus, String errorCode, String message) {
        return ResponseEntity.status(httpStatus).body(new ErrorResponse<>(errorCode, message, null));
    }
}
