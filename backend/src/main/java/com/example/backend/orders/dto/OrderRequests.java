package com.example.backend.orders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 주문 관련 요청 DTO 모음.
 * 각각 필드가 1~6개로 작아서 파일을 쪼개기보다 한 곳에 모아두는 편이 찾기 쉽다.
 */
public final class OrderRequests {

    private OrderRequests() {
    }

    /** POST /orders */
    @Getter
    public static class Create {
        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;
    }

    /** PATCH /orders/{orderId}/shipping */
    @Getter
    public static class Shipping {
        @NotBlank(message = "받는 사람은 필수입니다.")
        @Size(max = 50)
        private String receiverName;

        @NotBlank(message = "연락처는 필수입니다.")
        @Size(max = 20)
        private String receiverPhone;

        @NotBlank(message = "우편번호는 필수입니다.")
        @Size(max = 10)
        private String zipcode;

        @NotBlank(message = "기본 주소는 필수입니다.")
        @Size(max = 255)
        private String address1;

        @Size(max = 255)
        private String address2;

        @Size(max = 255)
        private String memo;
    }

    /** POST /orders/{orderId}/cancel */
    @Getter
    public static class Cancel {
        @Size(max = 255)
        private String reason;
    }

    /** POST /admin/orders/{orderId}/ship */
    @Getter
    public static class Ship {
        @NotBlank(message = "송장번호는 필수입니다.")
        @Size(max = 50)
        private String trackingNo;
    }
}
