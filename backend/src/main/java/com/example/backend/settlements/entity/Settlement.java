package com.example.backend.settlements.entity;

import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.orders.entity.Order;
import com.example.backend.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 판매자 정산.
 * <p>
 * 거래 확정(order CONFIRMED) 시점에 PENDING 상태로 자동 생성되고,
 * 관리자가 실제 송금 후 COMPLETED로 바꾼다.
 */
@Entity
@Table(
        name = "settlements",
        uniqueConstraints = @UniqueConstraint(name = "uk_settlements_order", columnNames = "order_id"),
        indexes = {
                @Index(name = "idx_settlements_seller", columnList = "seller_id"),
                @Index(name = "idx_settlements_admin", columnList = "admin_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    /** 플랫폼 수수료율 10% (명세 샘플: 89,000원 → 수수료 8,900원 → 실지급 80,100원) */
    public static final double FEE_RATE = 0.10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    /** 정산 처리 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    /** 판매가 */
    @Column(name = "gross_amount", nullable = false)
    private int grossAmount;

    /** 수수료 */
    @Column(name = "fee_amount", nullable = false)
    private int feeAmount;

    /** 실지급액 = gross - fee */
    @Column(name = "net_amount", nullable = false)
    private int netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private Settlement(Order order, User seller) {
        this.order = order;
        this.seller = seller;
        this.grossAmount = order.getOrderPrice();
        // 원 단위 정수라 반올림 없이 버림 처리한다 (수수료를 덜 떼는 쪽 = 판매자에게 유리)
        this.feeAmount = (int) (order.getOrderPrice() * FEE_RATE);
        this.netAmount = this.grossAmount - this.feeAmount;
        this.status = Status.PENDING;
    }

    /** 거래 확정 시 호출 */
    public static Settlement createFor(Order order) {
        return new Settlement(order, order.getProduct().getSeller());
    }

    public boolean isOwnedBy(Long userId) {
        return this.seller.getId().equals(userId);
    }

    /** 관리자 정산 완료 처리 */
    public void complete(User admin) {
        if (this.status == Status.COMPLETED) {
            throw new BusinessException(ErrorCode.SETTLEMENT_ALREADY_COMPLETED);
        }
        this.status = Status.COMPLETED;
        this.admin = admin;
        this.settledAt = LocalDateTime.now();
    }

    public enum Status {PENDING, COMPLETED}
}
