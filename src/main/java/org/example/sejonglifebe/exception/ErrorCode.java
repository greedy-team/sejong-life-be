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
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),
    REVIEW_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 좋아요입니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "파라미터 형식이 올바르지 않습니다."),
    MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다."),
    DUPLICATE_VALUE(HttpStatus.BAD_REQUEST, "중복된 값이 존재합니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    DUPLICATE_LIKE(HttpStatus.CONFLICT,"중복된 좋아요 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    PORTAL_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "포털 로그인 실패: 아이디 또는 비밀번호를 확인해주세요."),
    PORTAL_SSO_FAILED(HttpStatus.BAD_GATEWAY, "포털 SSO 연결에 실패했습니다."),
    PORTAL_NETWORK_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "포털 서버와의 통신 중 네트워크 오류가 발생했습니다."),
    PORTAL_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "포털 응답 데이터 처리 중 오류가 발생했습니다."),
    PORTAL_CLIENT_INIT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "포털 요청 클라이언트 초기화에 실패했습니다."),

    CATEGORY_NAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리명입니다.(룰렛)"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_AUTH_HEADER(HttpStatus.UNAUTHORIZED, "유효하지 않은 Authorization 헤더입니다."),
    INVALID_TOKEN(HttpStatus.FORBIDDEN, "인증에 실패했습니다."),
    USER_INFO_MISMATCH(HttpStatus.FORBIDDEN, "사용자 정보가 일치하지 않습니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;
}
