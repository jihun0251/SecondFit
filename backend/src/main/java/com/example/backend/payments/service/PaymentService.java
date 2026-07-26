package com.example.backend.payments.service;

import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.orders.entity.Order;
import com.example.backend.orders.repository.OrderRepository;
import com.example.backend.payments.dto.PaymentDtos;
import com.example.backend.payments.entity.Payment;
import com.example.backend.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 모의 결제.
     * 실 PG 연동 없이 결제 레코드만 만든다. 주문 1건당 결제 1건.
     */
    @Transactional
    public PaymentDtos.Created pay(Long buyerId, PaymentDtos.Request request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.isOwnedBy(buyerId)) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }
        // 프론트가 보낸 금액을 그대로 믿으면 안 된다. 서버가 가진 주문 금액과 대조한다.
        if (request.getAmount() != order.getOrderPrice()) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        Payment payment = paymentRepository.save(
                Payment.create(order, request.getAmount(), request.getMethod()));

        return PaymentDtos.Created.from(payment);
    }

    /** 결제 상세 — 구매자 본인 또는 ADMIN */
    public PaymentDtos.Detail getDetail(Long userId, boolean isAdmin, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!isAdmin && !payment.getOrder().isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.PAYMENT_FORBIDDEN);
        }

        return PaymentDtos.Detail.from(payment);
    }
}
