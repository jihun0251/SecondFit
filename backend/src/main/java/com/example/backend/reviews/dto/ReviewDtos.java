package com.example.backend.reviews.dto;

import com.example.backend.reviews.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/** 리뷰 요청/응답 DTO 모음 */
public final class ReviewDtos {

    private ReviewDtos() {
    }

    /** POST /reviews */
    @Getter
    public static class Request {
        @NotNull(message = "주문 ID는 필수입니다.")
        private Long orderId;

        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        private Integer rating;

        @Size(max = 1000, message = "리뷰는 1000자 이하여야 합니다.")
        private String content;
    }

    /** 작성 응답 → { reviewId, orderId, rating } */
    @Getter
    public static class Created {
        private final Long reviewId;
        private final Long orderId;
        private final int rating;

        private Created(Review r) {
            this.reviewId = r.getId();
            this.orderId = r.getOrder().getId();
            this.rating = r.getRating();
        }

        public static Created from(Review review) {
            return new Created(review);
        }
    }

    /** 리뷰 한 줄 */
    @Getter
    public static class Item {
        private final Long reviewId;
        private final String reviewer;
        private final int rating;
        private final String content;
        private final String productTitle;
        private final LocalDateTime createdAt;

        private Item(Review r) {
            this.reviewId = r.getId();
            this.reviewer = r.getReviewer().getNickname();
            this.rating = r.getRating();
            this.content = r.getContent();
            this.productTitle = r.getOrder().getProduct().getTitle();
            this.createdAt = r.getCreatedAt();
        }

        public static Item from(Review review) {
            return new Item(review);
        }
    }

    /**
     * GET /users/{userId}/reviews 응답.
     * 페이징 필드에 더해 평균 평점/총 개수를 함께 내려준다 (명세 샘플 기준).
     */
    @Getter
    public static class SellerReviews {
        private final double averageRating;
        private final long totalCount;
        private final List<Item> content;
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;

        public SellerReviews(double averageRating, long totalCount, List<Item> content,
                             int page, int size, long totalElements, int totalPages) {
            this.averageRating = averageRating;
            this.totalCount = totalCount;
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
    }
}
