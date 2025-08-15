package org.example.sejonglifebe.exception;

import org.example.sejonglifebe.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlaceNotFoundException.class)
    public ResponseEntity<ErrorResponse<Object>> handlePlaceNotFoundException(PlaceNotFoundException ex) {
        return ErrorResponse.of(HttpStatus.NOT_FOUND, "NOT_FOUND_PLACE", ex.getMessage());
    }
}
