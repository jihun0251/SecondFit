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

    // 회원/인증
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "USER_409_1", "이미 사용 중인 이메일입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "USER_409_2", "이미 사용 중인 닉네임입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "USER_401", "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "존재하지 않는 회원입니다."),
    USER_NOT_ACTIVE(HttpStatus.FORBIDDEN, "USER_403", "정지되었거나 탈퇴한 계정입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "만료된 토큰입니다."),

    // 카테고리
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_404", "존재하지 않는 카테고리입니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404", "존재하지 않는 상품입니다."),
    PRODUCT_NOT_EDITABLE(HttpStatus.CONFLICT, "PRODUCT_409_1", "입고 확인 후에는 수정할 수 없습니다."),
    PRODUCT_NOT_DELETABLE(HttpStatus.CONFLICT, "PRODUCT_409_2", "입고 확인 후에는 삭제할 수 없습니다."),
    PRODUCT_FORBIDDEN(HttpStatus.FORBIDDEN, "PRODUCT_403", "본인이 등록한 상품이 아닙니다."),
    PRODUCT_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "PRODUCT_400_1", "상품 이미지는 1장 이상 필요합니다."),
    PRODUCT_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PRODUCT_400_2", "상품 이미지는 최대 8장까지 등록할 수 있습니다."),
    PRODUCT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404_2", "존재하지 않는 상품 이미지입니다."),

    // 파일
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "FILE_400_1", "빈 파일입니다."),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FILE_400_2", "이미지 파일(jpg, jpeg, png, webp, gif)만 업로드할 수 있습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500", "파일 업로드에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
