package com.example.backend.products.dto;

import com.example.backend.products.entity.Product;
import lombok.Getter;

@Getter
public class ProductCreateResponse {

    private final Long productId;
    private final Product.Status status;

    private ProductCreateResponse(Long productId, Product.Status status) {
        this.productId = productId;
        this.status = status;
    }

    public static ProductCreateResponse from(Product product) {
        return new ProductCreateResponse(product.getId(), product.getStatus());
    }
}
