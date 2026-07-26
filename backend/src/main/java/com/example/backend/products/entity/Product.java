package com.example.backend.products.entity;

import com.example.backend.categories.entity.Category;
import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "products",
        // DDL의 인덱스와 1:1로 맞춘다. 인덱스가 없으면 목록 조회가 풀스캔으로 떨어진다.
        indexes = {
                @Index(name = "idx_products_seller", columnList = "seller_id"),
                @Index(name = "idx_products_category", columnList = "category_id"),
                @Index(name = "idx_products_status", columnList = "status"),
                @Index(name = "idx_products_created", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 판매자. LAZY = 실제로 seller를 꺼내 쓸 때만 users 테이블을 조회한다 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 원 단위 정수. 돈은 실수(double)로 다루지 않는다 */
    @Column(nullable = false)
    private int price;

    @Column(name = "size", length = 20)
    private String size;

    @Column(length = 30)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_grade", nullable = false, length = 20)
    private ConditionGrade conditionGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    /**
     * 신고 처리로 노출 중지된 상품.
     * 거래 상태(status)와는 별개 축으로 둔다 — 상태머신에 SUSPENDED를 끼워 넣으면
     * 모든 전이 규칙이 "그런데 정지 상태라면?"으로 오염되기 때문.
     */
    @Column(nullable = false)
    private boolean suspended = false;

    // --- AI 자동 태깅 제안값 (판매자가 수정하기 전 원본 예측을 그대로 보관) ---
    @Column(name = "ai_suggested_category", length = 50)
    private String aiSuggestedCategory;

    @Column(name = "ai_suggested_color", length = 30)
    private String aiSuggestedColor;

    @Column(name = "ai_confidence", precision = 5, scale = 4)
    private BigDecimal aiConfidence;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 상품 이미지들.
     * cascade = ALL      : 상품을 저장/삭제하면 이미지도 같이 저장/삭제
     * orphanRemoval=true : 리스트에서 빼면 DB에서도 삭제
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductImage> images = new ArrayList<>();

    @Builder
    private Product(User seller, Category category, String title, String description,
                    int price, String size, String color, ConditionGrade conditionGrade,
                    String aiSuggestedCategory, String aiSuggestedColor, BigDecimal aiConfidence) {
        this.seller = seller;
        this.category = category;
        this.title = title;
        this.description = description;
        this.price = price;
        this.size = size;
        this.color = color;
        this.conditionGrade = conditionGrade;
        this.aiSuggestedCategory = aiSuggestedCategory;
        this.aiSuggestedColor = aiSuggestedColor;
        this.aiConfidence = aiConfidence;
        this.status = Status.PENDING_INBOUND; // 등록 즉시 입고 대기
        this.viewCount = 0;
    }

    // ===================== 연관관계 편의 메서드 =====================

    /**
     * 양방향 연관관계는 양쪽 다 세팅해야 한다.
     * (자바 객체 그래프와 DB FK를 동시에 맞춰주기 위함)
     */
    public void addImage(ProductImage image) {
        this.images.add(image);
        image.assignProduct(this);
    }

    public void removeImage(ProductImage image) {
        this.images.remove(image); // orphanRemoval이 DELETE를 날려준다
    }

    // ===================== 비즈니스 로직 =====================

    public boolean isOwnedBy(Long userId) {
        return this.seller.getId().equals(userId);
    }

    /** 입고 확인 전에만 수정/삭제 가능 (v3 모델 규칙) */
    public boolean isEditable() {
        return this.status == Status.PENDING_INBOUND;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * 부분 수정(PATCH). null로 들어온 필드는 "변경 안 함"으로 취급한다.
     * 엔티티가 스스로 상태를 바꾸게 두는 편이 서비스에서 setter를 난사하는 것보다 안전하다.
     */
    public void update(Category category, String title, String description, Integer price,
                       String size, String color, ConditionGrade conditionGrade) {
        if (category != null) this.category = category;
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (size != null) this.size = size;
        if (color != null) this.color = color;
        if (conditionGrade != null) this.conditionGrade = conditionGrade;
    }

    // ===================== 상태 전이 =====================
    // 상태 머신: PENDING_INBOUND → ON_SALE → PAID → SHIPPED → DELIVERED → SETTLED
    //
    // 전이 규칙을 서비스 여기저기에 흩어놓으면 "어디서 상태가 바뀌었지?"를 추적할 수 없게 된다.
    // 엔티티가 자기 상태를 스스로 지키도록 모아둔다. 전이 조건이 안 맞으면 예외.

    /** 입고 확인 → 판매 시작 */
    public void startSelling() {
        requireStatus(Status.PENDING_INBOUND, ErrorCode.CONFLICT);
        this.status = Status.ON_SALE;
    }

    /** 주문 생성(결제) → 판매 중단 */
    public void markPaid() {
        requireStatus(Status.ON_SALE, ErrorCode.PRODUCT_NOT_ON_SALE);
        this.status = Status.PAID;
    }

    /** 주문 취소 → 다시 판매중으로 복귀 */
    public void returnToSale() {
        requireStatus(Status.PAID, ErrorCode.ORDER_NOT_CANCELABLE);
        this.status = Status.ON_SALE;
    }

    /** 본사 출고 */
    public void markShipped() {
        requireStatus(Status.PAID, ErrorCode.ORDER_NOT_SHIPPABLE);
        this.status = Status.SHIPPED;
    }

    /** 배송 완료 */
    public void markDelivered() {
        requireStatus(Status.SHIPPED, ErrorCode.ORDER_NOT_DELIVERABLE);
        this.status = Status.DELIVERED;
    }

    /** 거래 확정 → 정산 대상 */
    public void markSettled() {
        requireStatus(Status.DELIVERED, ErrorCode.ORDER_NOT_CONFIRMABLE);
        this.status = Status.SETTLED;
    }

    private void requireStatus(Status expected, ErrorCode errorCode) {
        if (this.status != expected) {
            throw new BusinessException(errorCode);
        }
    }

    /** 신고 처리(SUSPEND_PRODUCT)로 노출 중지 */
    public void suspend() {
        this.suspended = true;
    }

    /** 목록 화면에 쓸 대표 이미지 URL (대표 지정이 없으면 첫 장) */
    public String getThumbnailUrl() {
        return this.images.stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .or(() -> this.images.stream().findFirst())
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    public enum ConditionGrade {NEW, LIKE_NEW, GOOD, FAIR, POOR}

    /** 거래 상태 머신: 입고대기 → 판매중 → 결제완료 → 출고 → 배송완료 → 정산완료 */
    public enum Status {PENDING_INBOUND, ON_SALE, PAID, SHIPPED, DELIVERED, SETTLED}
}
