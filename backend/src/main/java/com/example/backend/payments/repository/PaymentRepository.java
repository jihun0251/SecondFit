package com.example.backend.payments.repository;

import com.example.backend.payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<Payment> findByOrderId(Long orderId);
}
