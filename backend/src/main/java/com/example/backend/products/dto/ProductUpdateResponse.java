package com.example.backend.products.dto;

import com.example.backend.products.entity.Product;
import lombok.Getter;

/**
 * 상품 수정 응답.
 * 명세서 기준: { "productId": 1042, "price": 85000, "status": "PENDING_INBOUND" }
 */
@Getter
public class ProductUpdateResponse {

    private final Long productId;
    private final int price;
    private final Product.Status status;

    private ProductUpdateResponse(Long productId, int price, Product.Status status) {
        this.productId = productId;
        this.price = price;
        this.status = status;
    }

    public static ProductUpdateResponse from(Product product) {
        return new ProductUpdateResponse(product.getId(), product.getPrice(), product.getStatus());
    }
}
