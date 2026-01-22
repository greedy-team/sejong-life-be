package org.example.sejonglifebe.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.common.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.example.sejonglifebe.exception.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SejongLifeException.class)
    public ResponseEntity<ErrorResponse<Void>> handleSejongLifeException(SejongLifeException exception, HttpServletRequest request) {

        log.warn("예외 발생: {}, method: {}, url: {}",
                exception.getMessage(),
                request.getMethod(),
                request.getRequestURL());

        return ErrorResponse.of(
                exception.getErrorCode().getHttpStatus(),
                exception.getErrorCode().name(),
                exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {

        log.warn("예외 발생: {}, method: {}, url: {}",
                exception.getMessage(),
                request.getMethod(),
                request.getRequestURL(),
                exception);

        return ErrorResponse.of(
                TYPE_MISMATCH.getHttpStatus(),
                TYPE_MISMATCH.name(),
                exception.getName() + " : " + TYPE_MISMATCH.getErrorMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMissingParam(MissingServletRequestParameterException exception, HttpServletRequest request) {
        log.warn("예외 발생: {}, method: {}, url: {}",
                exception.getMessage(),
                request.getMethod(),
                request.getRequestURL());

        return ErrorResponse.of(
                MISSING_REQUIRED_PARAMETER.getHttpStatus(),
                MISSING_REQUIRED_PARAMETER.name(),
                exception.getParameterName() + " : " + MISSING_REQUIRED_PARAMETER.getErrorMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("예외 발생: {},method:{}, url: {}",
                exception.getMessage(),
                request.getMethod(),
                request.getRequestURL());

        return ErrorResponse.of(
                DUPLICATE_VALUE.getHttpStatus(),
                DUPLICATE_VALUE.name(),
                DUPLICATE_VALUE.getErrorMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Void>> handleException(Exception exception, HttpServletRequest request) {
        log.error("예외 발생: {},method: {}, url: {}",
                exception.getMessage(),
                request.getMethod(),
                request.getRequestURL(),
                exception);

        return ErrorResponse.of(
                INTERNAL_SERVER_ERROR.getHttpStatus(),
                INTERNAL_SERVER_ERROR.name(),
                INTERNAL_SERVER_ERROR.getErrorMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<Void>> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String errorMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("예외 발생: {},method: {}, url: {}",
                errorMessage,
                request.getMethod(),
                request.getRequestURL(),
                exception);

        return ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getHttpStatus(),
                ErrorCode.INVALID_INPUT_VALUE.name(),
                errorMessage);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        return ErrorResponse.of(
                INVALID_AUTH_HEADER.getHttpStatus(),
                INVALID_AUTH_HEADER.name(),
                INVALID_AUTH_HEADER.getErrorMessage()
        );
    }
}
