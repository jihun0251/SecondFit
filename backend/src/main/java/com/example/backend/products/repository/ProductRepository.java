package com.example.backend.products.repository;

import com.example.backend.products.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * JpaSpecificationExecutor를 상속하면 findAll(Specification, Pageable)을 쓸 수 있다.
     * 선택 필터가 6개나 되는 검색은 JPQL에 (:param IS NULL OR ...)을 도배하는 것보다
     * Specification으로 자바에서 조건을 조립하는 쪽이 훨씬 읽기 쉽다.
     */

    /** 내가 등록한 상품 전체 (판매자 마이페이지) */
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    /** 내가 등록한 상품 중 특정 상태만 (GET /products/me?status=ON_SALE) */
    Page<Product> findBySellerIdAndStatus(Long sellerId, Product.Status status, Pageable pageable);

    /** 공개 프로필에 보여줄 판매중 상품 (최신 20건) */
    List<Product> findTop20BySellerIdAndStatusAndSuspendedFalseOrderByCreatedAtDesc(
            Long sellerId, Product.Status status);

    /** 카테고리별 판매중 상품 (GET /categories/{id}/products) */
    Page<Product> findByCategoryIdAndStatusAndSuspendedFalse(
            Long categoryId, Product.Status status, Pageable pageable);
}
