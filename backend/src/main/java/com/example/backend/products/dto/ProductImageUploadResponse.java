package com.example.backend.products.dto;

import com.example.backend.products.entity.ProductImage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 이미지 업로드 응답.
 * 명세서 기준: { "imageId": 5, "url": "https://.../5.jpg", "isThumbnail": true }
 */
@Getter
public class ProductImageUploadResponse {

    private final Long imageId;
    private final String url;

    // Jackson이 boolean 게터의 "is" 접두사를 떼버리므로 JSON 키를 명시한다
    @JsonProperty("isThumbnail")
    private final boolean thumbnail;

    private ProductImageUploadResponse(Long imageId, String url, boolean thumbnail) {
        this.imageId = imageId;
        this.url = url;
        this.thumbnail = thumbnail;
    }

    public static ProductImageUploadResponse from(ProductImage image) {
        return new ProductImageUploadResponse(image.getId(), image.getImageUrl(), image.isThumbnail());
    }
}
