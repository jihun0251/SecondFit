package com.example.backend.reviews.entity;

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
 * 거래 리뷰.
 * <p>
 * 거래 확정(order CONFIRMED)된 건에 대해 구매자가 판매자를 평가한다. 주문 1건당 리뷰 1건.
 */
@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_reviews_order", columnNames = "order_id"),
        indexes = @Index(name = "idx_reviews_seller", columnList = "seller_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** 리뷰 작성자 (구매자) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    /** 평가 대상 (판매자) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    /** 1~5 */
    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private Review(Order order, int rating, String content) {
        this.order = order;
        this.reviewer = order.getBuyer();
        this.seller = order.getProduct().getSeller();
        this.rating = rating;
        this.content = content;
    }

    /**
     * 리뷰 작성.
     * 거래 확정된 주문에만 허용한다 — 물건을 받지도 않고 평가하는 걸 막기 위함.
     */
    public static Review create(Order order, int rating, String content) {
        if (order.getStatus() != Order.Status.CONFIRMED) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);
        }
        return new Review(order, rating, content);
    }

    public boolean isWrittenBy(Long userId) {
        return this.reviewer.getId().equals(userId);
    }
}
