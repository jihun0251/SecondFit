package com.example.backend.orders.controller;

import com.example.backend.global.common.ApiResponse;
import com.example.backend.global.common.PageResponse;
import com.example.backend.orders.dto.OrderRequests;
import com.example.backend.orders.dto.OrderResponses;
import com.example.backend.orders.entity.Order;
import com.example.backend.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 관리자(본사) 출고 / 배송 처리 API */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 주문 목록 조회 (status: PAID / SHIPPED / DELIVERED / CONFIRMED / CANCELLED).
     * ⚠️ 명세서에 없는 추가 엔드포인트 — 관리자가 출고할 주문을 찾으려면 목록이 필요하다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponses.AdminItem>>> getOrders(
            @RequestParam(required = false) Order.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrdersForAdmin(status, page, size)));
    }

    /** 출고 처리 — 송장 등록. order PAID → SHIPPED, product PAID → SHIPPED */
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse<OrderResponses.Shipped>> ship(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderRequests.Ship request) {

        return ResponseEntity.ok(ApiResponse.success(orderService.ship(orderId, request)));
    }

    /** 배송 완료 처리 — order SHIPPED → DELIVERED, product SHIPPED → DELIVERED */
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponse<OrderResponses.Delivered>> deliver(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.deliver(orderId)));
    }
}
