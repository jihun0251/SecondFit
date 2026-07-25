package com.example.backend.categories.repository;

import com.example.backend.categories.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // parent가 NULL인 것 = 대분류만 조회
    // sort_order 오름차순으로 정렬
    List<Category> findByParentIsNullOrderBySortOrderAsc();
}