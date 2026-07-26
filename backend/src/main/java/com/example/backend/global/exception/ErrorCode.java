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

    // 입고
    INBOUND_NOT_FOUND(HttpStatus.NOT_FOUND, "INBOUND_404", "존재하지 않는 입고 건입니다."),
    INBOUND_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "INBOUND_409", "이미 입고 확인된 건입니다."),

    // 주문
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_404", "존재하지 않는 주문입니다."),
    ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "ORDER_403", "본인의 주문이 아닙니다."),
    SELF_PURCHASE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "ORDER_403_2", "본인이 등록한 상품은 구매할 수 없습니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.CONFLICT, "ORDER_409_1", "판매중인 상품이 아닙니다."),
    ORDER_NOT_CANCELABLE(HttpStatus.CONFLICT, "ORDER_409_2", "이미 출고되어 취소할 수 없습니다."),
    ORDER_NOT_CONFIRMABLE(HttpStatus.CONFLICT, "ORDER_409_3", "배송 완료 상태에서만 거래를 확정할 수 있습니다."),
    ORDER_NOT_SHIPPABLE(HttpStatus.CONFLICT, "ORDER_409_4", "결제 완료 상태에서만 출고할 수 있습니다."),
    ORDER_NOT_DELIVERABLE(HttpStatus.CONFLICT, "ORDER_409_5", "출고 상태에서만 배송 완료 처리할 수 있습니다."),
    SHIPPING_ADDRESS_REQUIRED(HttpStatus.CONFLICT, "ORDER_409_6", "배송지가 입력되지 않아 출고할 수 없습니다."),

    // 결제
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404", "존재하지 않는 결제입니다."),
    PAYMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "PAYMENT_403", "조회 권한이 없습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PAYMENT_409", "이미 결제된 주문입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_400", "결제 금액이 주문 금액과 일치하지 않습니다."),

    // 정산
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_404", "존재하지 않는 정산 건입니다."),
    SETTLEMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "SETTLEMENT_403", "조회 권한이 없습니다."),
    SETTLEMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "SETTLEMENT_409", "이미 정산 완료된 건입니다."),

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
