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
    DUPLICATE_LIKE(HttpStatus.CONFLICT, "중복된 좋아요 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    PORTAL_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "포털 로그인 실패: 아이디 또는 비밀번호를 확인해주세요."),
    PORTAL_CONNECTION_ERROR(HttpStatus.BAD_GATEWAY, "세종대학교 포털 서버에 접속할 수 없습니다."),
    PORTAL_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "포털 페이지에서 학생 정보를 파싱하는데 실패했습니다."),
    CATEGORY_NAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리명입니다.(룰렛)"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_AUTH_HEADER(HttpStatus.UNAUTHORIZED, "유효하지 않은 Authorization 헤더입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "잘못된 형식의 토큰입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 파일을 처리하지 못했습니다."),
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3에 파일 업로드 중 오류가 발생했습니다."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 최대 파일 크기를 초과했습니다."),
    IMAGE_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 변환에 실패했습니다."),
    S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 삭제 중 오류가 발생했습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "리뷰 수정, 삭제 권한이 없습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    ALREADY_FAVORITE_PLACE(HttpStatus.BAD_REQUEST, "이미 즐겨찾기로 등록된 장소입니다.");
    private final HttpStatus httpStatus;
    private final String errorMessage;
}
