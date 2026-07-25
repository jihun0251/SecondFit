package com.example.backend.categories.dto;

import com.example.backend.categories.entity.Category;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryResponse {

    private final Long id;
    private final String name;
    private final List<CategoryResponse> children;

    // 엔티티 → DTO 변환
    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.children = category.getChildren().stream()
                .map(CategoryResponse::new)   // 자식들도 각각 DTO로 변환
                .toList();
    }
}