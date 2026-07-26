package com.example.backend.orders.controller;

import com.example.backend.global.common.ApiResponse;
import com.example.backend.orders.dto.OrderRequests;
import com.example.backend.orders.dto.OrderResponses;
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
