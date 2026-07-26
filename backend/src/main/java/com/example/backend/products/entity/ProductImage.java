package com.example.backend.products.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_images",
        indexes = @Index(name = "idx_product_images_product", columnList = "product_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /** AI 추론 대상이 되는 대표 이미지 여부 */
    @Column(name = "is_thumbnail", nullable = false)
    private boolean thumbnail = false;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private ProductImage(String imageUrl, boolean thumbnail, int sortOrder) {
        this.imageUrl = imageUrl;
        this.thumbnail = thumbnail;
        this.sortOrder = sortOrder;
    }

    /** Product.addImage()에서만 호출한다 (연관관계 편의 메서드 전용) */
    void assignProduct(Product product) {
        this.product = product;
    }

    public void markAsThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }
}
