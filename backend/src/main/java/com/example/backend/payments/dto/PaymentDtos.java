package com.example.backend.payments.dto;

import com.example.backend.payments.entity.Payment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

/** 결제 요청/응답 DTO */
public final class PaymentDtos {

    private PaymentDtos() {
    }

    /** POST /payments */
    @Getter
    public static class Request {
        @NotNull(message = "주문 ID는 필수입니다.")
        private Long orderId;

        @NotNull(message = "결제 금액은 필수입니다.")
        @Min(value = 0, message = "결제 금액은 0원 이상이어야 합니다.")
        private Integer amount;

        @NotNull(message = "결제 수단은 필수입니다.")
        private Payment.Method method;
    }

    /** 결제 완료 응답 → { paymentId, orderId, status, pgTid } */
    @Getter
    public static class Created {
        private final Long paymentId;
        private final Long orderId;
        private final Payment.Status status;
        private final String pgTid;

        private Created(Payment p) {
            this.paymentId = p.getId();
            this.orderId = p.getOrder().getId();
            this.status = p.getStatus();
            this.pgTid = p.getPgTid();
        }

        public static Created from(Payment payment) {
            return new Created(payment);
        }
    }

    /** 결제 상세 → { paymentId, orderId, amount, method, status, paidAt } */
    @Getter
    public static class Detail {
        private final Long paymentId;
        private final Long orderId;
        private final int amount;
        private final Payment.Method method;
        private final Payment.Status status;
        private final LocalDateTime paidAt;

        private Detail(Payment p) {
            this.paymentId = p.getId();
            this.orderId = p.getOrder().getId();
            this.amount = p.getAmount();
            this.method = p.getMethod();
            this.status = p.getStatus();
            this.paidAt = p.getPaidAt();
        }

        public static Detail from(Payment payment) {
            return new Detail(payment);
        }
    }
}
