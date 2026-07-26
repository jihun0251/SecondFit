package com.example.backend.products.dto;

import com.example.backend.products.entity.Product;
import lombok.Getter;

/** 목록 화면(카드 그리드)용 축약 응답 */
@Getter
public class ProductSummaryResponse {

    private final Long productId;
    private final String title;
    private final int price;
    private final String size;
    private final Product.ConditionGrade conditionGrade;
    private final String thumbnail;
    private final Product.Status status;

    private ProductSummaryResponse(Product product) {
        this.productId = product.getId();
        this.title = product.getTitle();
        this.price = product.getPrice();
        this.size = product.getSize();
        this.conditionGrade = product.getConditionGrade();
        this.thumbnail = product.getThumbnailUrl();
        this.status = product.getStatus();
    }

    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(product);
    }
}
