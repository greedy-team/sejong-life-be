package org.example.sejonglifebe.exception;

import lombok.Getter;

@Getter
public class SejongLifeException extends RuntimeException {

    private final ErrorCode errorCode;

    public SejongLifeException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public SejongLifeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.errorCode = errorCode;
    }
}
