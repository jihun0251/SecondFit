package com.example.backend.products.dto;

import com.example.backend.products.entity.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GET /products 의 쿼리 파라미터 묶음.
 * 컨트롤러에서 @ModelAttribute로 받으면 스프링이 setter로 채워준다.
 * (파라미터가 많아질 때 메서드 시그니처가 터지는 걸 막는 패턴)
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductSearchCondition {

    private String keyword;
    private Long categoryId;
    private Integer minPrice;
    private Integer maxPrice;
    /** 의류 사이즈 (S/M/L/XL) — 페이지 크기(pageSize)와 헷갈리지 말 것 */
    private String size;
    private Product.ConditionGrade conditionGrade;

    /** latest(기본) | price_asc | price_desc */
    private String sort = "latest";
    private int page = 0;
    private int pageSize = 20;
}
