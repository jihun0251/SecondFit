package com.example.backend.categories.service;

import com.example.backend.categories.dto.CategoryResponse;
import com.example.backend.categories.entity.Category;
import com.example.backend.categories.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getCategoryTree() {
        // 1. 대분류만 DB에서 꺼낸다 (자식은 각 대분류의 children에 딸려옴)
        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderBySortOrderAsc();

        // 2. 각 대분류를 CategoryResponse로 변환 → 자식도 재귀적으로 함께 변환됨
        return rootCategories.stream()
                .map(CategoryResponse::new)
                .toList();
    }
}