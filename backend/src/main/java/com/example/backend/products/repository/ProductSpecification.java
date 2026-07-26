package com.example.backend.products.repository;

import com.example.backend.products.dto.ProductSearchCondition;
import com.example.backend.products.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 상품 검색 조건 조립기.
 * <p>
 * Specification은 "WHERE 절 조각"을 자바 객체로 만든 것이다.
 * 값이 들어온 필터만 predicate 리스트에 추가하고 마지막에 AND로 묶는다.
 */
public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> search(ProductSearchCondition cond) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 목록에는 판매중인 상품만 노출한다 (입고 대기/결제완료 등은 숨김)
            predicates.add(cb.equal(root.get("status"), Product.Status.ON_SALE));
            // 신고 처리로 노출 중지된 상품은 제외
            predicates.add(cb.isFalse(root.get("suspended")));

            if (StringUtils.hasText(cond.getKeyword())) {
                predicates.add(cb.like(root.get("title"), "%" + cond.getKeyword().trim() + "%"));
            }
            if (cond.getCategoryId() != null) {
                // root.get("category").get("id")는 조인 없이 FK 컬럼(category_id)만 비교한다
                predicates.add(cb.equal(root.get("category").get("id"), cond.getCategoryId()));
            }
            if (cond.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), cond.getMinPrice()));
            }
            if (cond.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), cond.getMaxPrice()));
            }
            if (StringUtils.hasText(cond.getSize())) {
                predicates.add(cb.equal(root.get("size"), cond.getSize()));
            }
            if (cond.getConditionGrade() != null) {
                predicates.add(cb.equal(root.get("conditionGrade"), cond.getConditionGrade()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
