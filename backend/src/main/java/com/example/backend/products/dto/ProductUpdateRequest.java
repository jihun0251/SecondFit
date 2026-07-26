package com.example.backend.products.dto;

import com.example.backend.products.entity.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * PATCH 요청이므로 모든 필드가 선택이다.
 * null = "이 필드는 건드리지 않음"을 의미한다.
 */
@Getter
public class ProductUpdateRequest {

    private Long categoryId;

    @Size(max = 150, message = "상품명은 150자 이하여야 합니다.")
    private String title;

    private String description;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @Size(max = 20, message = "사이즈는 20자 이하여야 합니다.")
    private String size;

    @Size(max = 30, message = "색상은 30자 이하여야 합니다.")
    private String color;

    private Product.ConditionGrade conditionGrade;
}
