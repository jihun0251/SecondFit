package com.example.backend.wishlists.entity;

import com.example.backend.products.entity.Product;
import com.example.backend.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** 찜 (사용자 N : M 상품) */
@Entity
@Table(
        name = "wishlists",
        uniqueConstraints = @UniqueConstraint(name = "uk_wishlists_user_product",
                columnNames = {"user_id", "product_id"}),
        indexes = @Index(name = "idx_wishlists_product", columnList = "product_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 찜한 시점의 가격.
     * 이걸 저장해 둬야 나중에 "찜한 뒤 6,000원 내려갔어요" 같은 가격 변동을 계산할 수 있다.
     */
    @Column(name = "price_at_wish", nullable = false)
    private int priceAtWish;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private Wishlist(User user, Product product) {
        this.user = user;
        this.product = product;
        this.priceAtWish = product.getPrice();
    }

    public static Wishlist create(User user, Product product) {
        return new Wishlist(user, product);
    }

    /** 현재가 - 찜한 시점 가격 (음수면 인하) */
    public int getPriceChange() {
        return this.product.getPrice() - this.priceAtWish;
    }
}
