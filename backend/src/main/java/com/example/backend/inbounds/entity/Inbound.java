package com.example.backend.inbounds.entity;

import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.products.entity.Product;
import com.example.backend.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 본사 입고 기록.
 * <p>
 * 상품 등록 시 AWAITING 상태로 1건 자동 생성되고,
 * 관리자가 실물을 받아 확인하면 CONFIRMED로 전이되면서 상품이 판매중이 된다.
 * (v3 = 검수 없는 모델이라 입고 확인이 곧 판매 시작)
 */
@Entity
@Table(
        name = "inbounds",
        uniqueConstraints = @UniqueConstraint(name = "uk_inbounds_product", columnNames = "product_id"),
        indexes = @Index(name = "idx_inbounds_admin", columnList = "admin_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inbound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 상품당 입고 1건 (uk_inbounds_product) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** 입고 확인을 처리한 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    /** 판매자 → 본사 발송 송장번호 */
    @Column(name = "tracking_no", length = 50)
    private String trackingNo;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private Inbound(Product product) {
        this.product = product;
        this.status = Status.AWAITING;
    }

    /** 상품 등록 시 자동으로 입고 대기 건을 만든다 */
    public static Inbound createFor(Product product) {
        return new Inbound(product);
    }

    public void registerTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    /**
     * 관리자 입고 확인.
     * 입고 확인과 상품 판매 시작은 반드시 같이 일어나야 하므로 여기서 함께 처리한다.
     */
    public void confirm(User admin) {
        if (this.status == Status.CONFIRMED) {
            throw new BusinessException(ErrorCode.INBOUND_ALREADY_CONFIRMED);
        }
        this.status = Status.CONFIRMED;
        this.admin = admin;
        this.confirmedAt = LocalDateTime.now();
        this.product.startSelling(); // PENDING_INBOUND → ON_SALE
    }

    public enum Status {AWAITING, CONFIRMED}
}
