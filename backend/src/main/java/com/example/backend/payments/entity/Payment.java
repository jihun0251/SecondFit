package com.example.backend.payments.entity;

import com.example.backend.orders.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 결제 (모의).
 * <p>
 * 실 PG 연동 없이 상태만 전이시킨다. 주문과 1:1.
 */
@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payments_order", columnNames = "order_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    private static final DateTimeFormatter TID_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    /** 모의 거래 식별자 (실 PG의 거래번호 자리) */
    @Column(name = "pg_tid", length = 100)
    private String pgTid;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private Payment(Order order, int amount, Method method) {
        this.order = order;
        this.amount = amount;
        this.method = method;
        this.status = Status.PAID;
        this.paidAt = LocalDateTime.now();
        this.pgTid = generateMockTid(order.getId());
    }

    public static Payment create(Order order, int amount, Method method) {
        return new Payment(order, amount, method);
    }

    /** 예: MOCK-20260726-9931 */
    private static String generateMockTid(Long orderId) {
        return "MOCK-" + LocalDateTime.now().format(TID_DATE) + "-" + String.format("%04d", orderId % 10000);
    }

    /** 주문 취소 시 환불 처리 */
    public void refund() {
        this.status = Status.REFUNDED;
    }

    public enum Method {CARD, BANK_TRANSFER, MOCK}

    public enum Status {PAID, REFUNDED, FAILED}
}
