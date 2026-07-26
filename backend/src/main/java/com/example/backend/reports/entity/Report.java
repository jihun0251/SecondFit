package com.example.backend.reports.entity;

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

/** 부적절 게시물 신고 */
@Entity
@Table(
        name = "reports",
        indexes = {
                @Index(name = "idx_reports_reporter", columnList = "reporter_id"),
                @Index(name = "idx_reports_product", columnList = "product_id"),
                @Index(name = "idx_reports_admin", columnList = "admin_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    /** 관리자 조치: 상품 노출 중지 */
    public static final String ACTION_SUSPEND_PRODUCT = "SUSPEND_PRODUCT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /** 처리 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Reason reason;

    @Column(length = 500)
    private String detail;

    /** 관리자가 취한 조치 (예: SUSPEND_PRODUCT) */
    @Column(length = 50)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    private Report(User reporter, Product product, Reason reason, String detail) {
        this.reporter = reporter;
        this.product = product;
        this.reason = reason;
        this.detail = detail;
        this.status = Status.RECEIVED;
    }

    public static Report create(User reporter, Product product, Reason reason, String detail) {
        if (product.isOwnedBy(reporter.getId())) {
            throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }
        return new Report(reporter, product, reason, detail);
    }

    /**
     * 관리자 처리.
     * RESOLVED + action=SUSPEND_PRODUCT면 대상 상품을 노출 중지시킨다.
     */
    public void handle(User admin, Status newStatus, String action) {
        if (this.status == Status.RESOLVED || this.status == Status.REJECTED) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_HANDLED);
        }

        this.status = newStatus;
        this.admin = admin;
        this.action = action;

        if (newStatus == Status.RESOLVED || newStatus == Status.REJECTED) {
            this.resolvedAt = LocalDateTime.now();
        }
        if (newStatus == Status.RESOLVED && ACTION_SUSPEND_PRODUCT.equals(action) && this.product != null) {
            this.product.suspend();
        }
    }

    /**
     * 신고 대상 상품이 삭제될 때 참조만 끊는다.
     * 신고 기록 자체는 남겨야 한다 — 반복 신고자 추적이나 처리 이력 확인에 쓰이기 때문.
     * (DDL의 reports.product_id ON DELETE SET NULL과 같은 동작)
     */
    public void detachProduct() {
        this.product = null;
    }

    public enum Reason {FAKE, ABUSE, PROHIBITED, SPAM, ETC}

    public enum Status {RECEIVED, REVIEWING, RESOLVED, REJECTED}
}
