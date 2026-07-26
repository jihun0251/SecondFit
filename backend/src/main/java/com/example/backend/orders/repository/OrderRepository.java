package com.example.backend.orders.repository;

import com.example.backend.orders.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatus(Long buyerId, Order.Status status, Pageable pageable);

    /** 관리자 주문 목록 (상태 필터) */
    Page<Order> findByStatus(Order.Status status, Pageable pageable);

    /**
     * 판매자의 거래 완료 건수 (프로필의 tradeCount).
     * orders에는 seller 컬럼이 없으므로 product를 거쳐 seller를 찾아간다.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.product.seller.id = :sellerId AND o.status = :status")
    long countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") Order.Status status);
}
