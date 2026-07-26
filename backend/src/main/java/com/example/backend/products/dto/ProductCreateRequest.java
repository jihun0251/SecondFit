package com.example.backend.products.dto;

import com.example.backend.products.entity.Product;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ProductCreateRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 150, message = "상품명은 150자 이하여야 합니다.")
    private String title;

    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @Size(max = 20, message = "사이즈는 20자 이하여야 합니다.")
    private String size;

    @Size(max = 30, message = "색상은 30자 이하여야 합니다.")
    private String color;

    @NotNull(message = "상품 상태 등급은 필수입니다.")
    private Product.ConditionGrade conditionGrade;

    /** 이미지 URL 목록. 먼저 업로드 API로 URL을 받아온 뒤 여기에 담아 보낸다 */
    @NotEmpty(message = "상품 이미지는 1장 이상 필요합니다.")
    @Size(max = 8, message = "상품 이미지는 최대 8장까지 등록할 수 있습니다.")
    private List<@NotBlank String> images;

    /** 대표 이미지 인덱스 (기본 0) */
    @Min(value = 0, message = "대표 이미지 인덱스는 0 이상이어야 합니다.")
    private Integer thumbnailIndex = 0;

    // --- AI 태깅 원본 예측값 (판매자가 값을 고쳐도 원본은 그대로 보관) ---
    private String aiSuggestedCategory;
    private String aiSuggestedColor;

    @DecimalMin(value = "0.0", message = "AI 신뢰도는 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", message = "AI 신뢰도는 1 이하여야 합니다.")
    private BigDecimal aiConfidence;
}
