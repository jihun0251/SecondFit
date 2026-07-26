package com.example.backend.products.dto;

import com.example.backend.categories.entity.Category;
import com.example.backend.products.entity.Product;
import com.example.backend.products.entity.ProductImage;
import com.example.backend.users.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

/** 상세 화면용 응답 (이미지 + 카테고리 + 판매자 포함) */
@Getter
public class ProductDetailResponse {

    private final Long productId;
    private final String title;
    private final String description;
    private final int price;
    private final String size;
    private final String color;
    private final Product.ConditionGrade conditionGrade;
    private final CategoryInfo category;
    private final Product.Status status;
    private final int viewCount;
    private final List<ImageInfo> images;
    private final SellerInfo seller;

    private ProductDetailResponse(Product p) {
        this.productId = p.getId();
        this.title = p.getTitle();
        this.description = p.getDescription();
        this.price = p.getPrice();
        this.size = p.getSize();
        this.color = p.getColor();
        this.conditionGrade = p.getConditionGrade();
        this.category = p.getCategory() == null ? null : CategoryInfo.from(p.getCategory());
        this.status = p.getStatus();
        this.viewCount = p.getViewCount();
        this.images = p.getImages().stream().map(ImageInfo::from).toList();
        this.seller = SellerInfo.from(p.getSeller());
    }

    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(product);
    }

    @Getter
    public static class CategoryInfo {
        private final Long id;
        private final String name;

        private CategoryInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        private static CategoryInfo from(Category c) {
            return new CategoryInfo(c.getId(), c.getName());
        }
    }

    @Getter
    public static class ImageInfo {
        private final Long imageId;
        private final String url;
        // Jackson은 boolean 게터의 "is" 접두사를 떼버려서 그냥 두면 JSON 키가 "thumbnail"이 된다.
        // 명세서(isThumbnail)와 맞추기 위해 이름을 명시한다.
        @JsonProperty("isThumbnail")
        private final boolean thumbnail;

        private ImageInfo(Long imageId, String url, boolean thumbnail) {
            this.imageId = imageId;
            this.url = url;
            this.thumbnail = thumbnail;
        }

        private static ImageInfo from(ProductImage i) {
            return new ImageInfo(i.getId(), i.getImageUrl(), i.isThumbnail());
        }
    }

    @Getter
    public static class SellerInfo {
        private final Long userId;
        private final String nickname;
        /** 판매자 평점 — reviews 도메인 구현 전까지는 null */
        private final Double rating;

        private SellerInfo(Long userId, String nickname, Double rating) {
            this.userId = userId;
            this.nickname = nickname;
            this.rating = rating;
        }

        private static SellerInfo from(User u) {
            return new SellerInfo(u.getId(), u.getNickname(), null);
        }
    }
}
