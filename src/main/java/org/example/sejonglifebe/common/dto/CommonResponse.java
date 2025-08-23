package org.example.sejonglifebe.common.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record CommonResponse<T>(
        String message,
        T data
) {

    public static <T> ResponseEntity<CommonResponse<T>> of(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(new CommonResponse<>(message, data));
    }
}
