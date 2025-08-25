package org.example.sejonglifebe.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장소입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 태그입니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "파라미터 형식이 올바르지 않습니다."),
    MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다."),
    DUPLICATE_VALUE(HttpStatus.BAD_REQUEST, "중복된 값이 존재합니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    PORTAL_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "포털 로그인 실패: 아이디 또는 비밀번호를 확인해주세요."),
    PORTAL_CONNECTION_ERROR(HttpStatus.BAD_GATEWAY, "세종대학교 포털 서버에 접속할 수 없습니다.");
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    CATEGORY_NAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리명입니다.(룰렛)");

    private final HttpStatus httpStatus;
    private final String errorMessage;
}
