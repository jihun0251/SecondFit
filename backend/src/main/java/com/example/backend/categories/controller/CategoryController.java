package com.example.backend.categories.controller;

import com.example.backend.categories.dto.CategoryResponse;
import com.example.backend.categories.service.CategoryService;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.global.common.PageResponse;
import com.example.backend.products.dto.ProductSummaryResponse;
import com.example.backend.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    /** 카테고리 트리 (좌측 필터 패널용) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categories = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 카테고리별 판매중 상품 목록.
     * ⚠️ 페이지 크기 파라미터가 GET /products는 pageSize, 여기는 size다 (명세서 기준).
     */
    @GetMapping("/{categoryId}/products")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> getCategoryProducts(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getByCategory(categoryId, sort, page, size)));
    }
}
