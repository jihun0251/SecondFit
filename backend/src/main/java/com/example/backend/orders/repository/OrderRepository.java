package com.example.backend.orders.repository;

import com.example.backend.orders.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatus(Long buyerId, Order.Status status, Pageable pageable);
}
