package com.example.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "COMMON_409", "요청이 현재 상태와 충돌합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류입니다."),

    // 회원/인증 (도메인별로 추가해 나가면 됨)
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "USER_409_1", "이미 사용 중인 이메일입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "USER_409_2", "이미 사용 중인 닉네임입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "USER_401", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404", "존재하지 않는 상품입니다."),
    PRODUCT_NOT_EDITABLE(HttpStatus.CONFLICT, "PRODUCT_409", "입고 확인 후에는 수정할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}