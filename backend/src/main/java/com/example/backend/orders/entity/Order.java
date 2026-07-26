package com.example.backend.orders.entity;

import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.products.entity.Product;
import com.example.backend.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 주문.
 * <p>
 * 상태 머신: PAID → SHIPPED → DELIVERED → CONFIRMED
 * ····················└→ CANCELLED (출고 전까지만)
 * <p>
 * 상품이 단일 재고라 주문도 상품당 1건이다 (uk_orders_product).
 */
@Entity
@Table(
        name = "orders",
        // ⚠️ DDL에는 uk_orders_product(상품당 주문 1건) 유니크 제약이 있었으나 제거했다.
        //    주문 취소 시 상품이 ON_SALE로 복귀하므로 같은 상품에 두 번째 주문이 생길 수 있는데,
        //    유니크 제약이 있으면 그 재주문이 DB 레벨에서 막혀버린다.
        //    "동시에 두 명이 사는 것"은 Product.markPaid()의 ON_SALE 상태 검사로 막는다.
        indexes = {
                @Index(name = "idx_orders_product", columnList = "product_id"),
                @Index(name = "idx_orders_buyer", columnList = "buyer_id"),
                @Index(name = "idx_orders_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    /**
     * 결제 시점 가격 스냅샷.
     * 판매자가 나중에 가격을 바꿔도 이미 맺어진 거래 금액은 변하면 안 되므로 복사해 둔다.
     */
    @Column(name = "order_price", nullable = false)
    private int orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    // --- 배송지 (주문 생성 시점엔 비어 있고 PATCH /shipping에서 채워진다) ---
    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(length = 10)
    private String zipcode;

    @Column(length = 255)
    private String address1;

    @Column(length = 255)
    private String address2;

    @Column(length = 255)
    private String memo;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    /** 본사 → 구매자 출고 송장 */
    @Column(name = "tracking_no", length = 50)
    private String trackingNo;

    // --- 상태 타임라인 ---
    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Order(Product product, User buyer) {
        this.product = product;
        this.buyer = buyer;
        this.orderPrice = product.getPrice(); // 가격 스냅샷
        this.status = Status.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 주문 생성.
     * 명세상 주문은 생성 즉시 PAID이며 상품도 함께 PAID로 넘어간다.
     */
    public static Order create(Product product, User buyer) {
        if (product.isOwnedBy(buyer.getId())) {
            throw new BusinessException(ErrorCode.SELF_PURCHASE_NOT_ALLOWED);
        }
        product.markPaid(); // ON_SALE이 아니면 여기서 예외
        return new Order(product, buyer);
    }

    // ===================== 권한 =====================

    public boolean isOwnedBy(Long userId) {
        return this.buyer.getId().equals(userId);
    }

    // ===================== 상태 전이 =====================

    /** 배송지 입력/수정. 상태는 PAID 유지 (출고는 관리자가) */
    public void updateShipping(String receiverName, String receiverPhone, String zipcode,
                               String address1, String address2, String memo) {
        if (this.status != Status.PAID) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipcode = zipcode;
        this.address1 = address1;
        this.address2 = address2;
        this.memo = memo;
    }

    /** 관리자 출고 처리 */
    public void ship(String trackingNo) {
        requireStatus(Status.PAID, ErrorCode.ORDER_NOT_SHIPPABLE);
        // 배송지가 없으면 물건을 보낼 수가 없다
        if (this.receiverName == null || this.address1 == null) {
            throw new BusinessException(ErrorCode.SHIPPING_ADDRESS_REQUIRED);
        }
        this.status = Status.SHIPPED;
        this.trackingNo = trackingNo;
        this.shippedAt = LocalDateTime.now();
        this.product.markShipped();
    }

    /** 배송 완료 처리 */
    public void deliver() {
        requireStatus(Status.SHIPPED, ErrorCode.ORDER_NOT_DELIVERABLE);
        this.status = Status.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.product.markDelivered();
    }

    /** 구매자 거래 확정 → 정산 대상이 된다 */
    public void confirm() {
        requireStatus(Status.DELIVERED, ErrorCode.ORDER_NOT_CONFIRMABLE);
        this.status = Status.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.product.markSettled();
    }

    /** 주문 취소 (출고 전까지만). 상품은 다시 판매중으로 돌아간다 */
    public void cancel(String reason) {
        requireStatus(Status.PAID, ErrorCode.ORDER_NOT_CANCELABLE);
        this.status = Status.CANCELLED;
        this.cancelReason = reason;
        this.product.returnToSale();
    }

    private void requireStatus(Status expected, ErrorCode errorCode) {
        if (this.status != expected) {
            throw new BusinessException(errorCode);
        }
    }

    public enum Status {PAID, SHIPPED, DELIVERED, CONFIRMED, CANCELLED}
}
