package com.example.backend.orders.service;

import com.example.backend.global.common.PageResponse;
import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.orders.dto.OrderDetailResponse;
import com.example.backend.orders.dto.OrderRequests;
import com.example.backend.orders.dto.OrderResponses;
import com.example.backend.orders.entity.Order;
import com.example.backend.orders.repository.OrderRepository;
import com.example.backend.payments.entity.Payment;
import com.example.backend.payments.repository.PaymentRepository;
import com.example.backend.products.entity.Product;
import com.example.backend.products.repository.ProductRepository;
import com.example.backend.settlements.entity.Settlement;
import com.example.backend.settlements.repository.SettlementRepository;
import com.example.backend.users.entity.User;
import com.example.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;

    // ===================== 구매자 =====================

    /**
     * 주문 생성.
     * 상품이 ON_SALE인지, 본인 상품은 아닌지 검사는 Order.create()가 담당한다.
     */
    @Transactional
    public OrderResponses.Created create(Long buyerId, OrderRequests.Create request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.save(Order.create(product, buyer));
        return OrderResponses.Created.from(order);
    }

    /** 주문 상세 — 구매자 본인 또는 ADMIN */
    public OrderDetailResponse getDetail(Long userId, boolean isAdmin, Long orderId) {
        Order order = findOrder(orderId);
        requireBuyerOrAdmin(order, userId, isAdmin);
        return OrderDetailResponse.from(order);
    }

    /** 내 구매 내역 */
    public PageResponse<OrderResponses.Summary> getMyOrders(Long buyerId, Order.Status status,
                                                            int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                size <= 0 ? 20 : Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "paidAt")
        );

        Page<Order> result = (status == null)
                ? orderRepository.findByBuyerId(buyerId, pageable)
                : orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable);

        return PageResponse.of(result, OrderResponses.Summary::from);
    }

    /** 배송지 입력/수정 */
    @Transactional
    public OrderResponses.ShippingSaved updateShipping(Long buyerId, Long orderId,
                                                       OrderRequests.Shipping request) {
        Order order = findOrder(orderId);
        requireBuyer(order, buyerId);

        order.updateShipping(request.getReceiverName(), request.getReceiverPhone(),
                request.getZipcode(), request.getAddress1(), request.getAddress2(), request.getMemo());

        return OrderResponses.ShippingSaved.from(order);
    }

    /**
     * 거래 확정.
     * order DELIVERED → CONFIRMED, product DELIVERED → SETTLED, 그리고 정산(PENDING) 생성.
     * 이 셋은 하나라도 실패하면 전부 취소되어야 하므로 같은 트랜잭션 안에서 처리한다.
     */
    @Transactional
    public OrderResponses.Confirmed confirm(Long buyerId, Long orderId) {
        Order order = findOrder(orderId);
        requireBuyer(order, buyerId);

        order.confirm();

        Settlement settlement = settlementRepository.save(Settlement.createFor(order));

        return OrderResponses.Confirmed.of(order, settlement.getId());
    }

    /**
     * 주문 취소 (출고 전까지만).
     * 상품은 ON_SALE로 되돌리고, 결제 건이 있으면 환불 처리한다.
     */
    @Transactional
    public OrderResponses.Cancelled cancel(Long userId, boolean isAdmin, Long orderId,
                                           OrderRequests.Cancel request) {
        Order order = findOrder(orderId);
        requireBuyerOrAdmin(order, userId, isAdmin);

        order.cancel(request == null ? null : request.getReason());

        paymentRepository.findByOrderId(orderId).ifPresent(Payment::refund);

        return OrderResponses.Cancelled.from(order);
    }

    // ===================== 관리자 =====================

    /** 출고 처리 (송장 등록) */
    @Transactional
    public OrderResponses.Shipped ship(Long orderId, OrderRequests.Ship request) {
        Order order = findOrder(orderId);
        order.ship(request.getTrackingNo());
        return OrderResponses.Shipped.from(order);
    }

    /** 배송 완료 처리 */
    @Transactional
    public OrderResponses.Delivered deliver(Long orderId) {
        Order order = findOrder(orderId);
        order.deliver();
        return OrderResponses.Delivered.from(order);
    }

    // ===================== 공통 =====================

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void requireBuyer(Order order, Long userId) {
        if (!order.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }
    }

    private void requireBuyerOrAdmin(Order order, Long userId, boolean isAdmin) {
        if (!isAdmin && !order.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }
    }
}
