package com.example.backend.orders.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.global.common.PageResponse;
import com.example.backend.orders.dto.OrderDetailResponse;
import com.example.backend.orders.dto.OrderRequests;
import com.example.backend.orders.dto.OrderResponses;
import com.example.backend.orders.entity.Order;
import com.example.backend.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 주문 생성(결제 요청) — 생성 즉시 PAID, 상품은 ON_SALE → PAID */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponses.Created>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OrderRequests.Create request) {

        OrderResponses.Created response = orderService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 내 구매 내역.
     * ⚠️ /{orderId} 매핑보다 위에 있어야 "me"가 orderId로 해석되지 않는다.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponses.Summary>>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Order.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.getMyOrders(principal.getUserId(), status, page, size)));
    }

    /** 주문 상세 — 구매자 본인 또는 ADMIN */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.getDetail(principal.getUserId(), principal.isAdmin(), orderId)));
    }

    /** 배송지 입력/수정 — 상태는 PAID 유지 (출고는 관리자가) */
    @PatchMapping("/{orderId}/shipping")
    public ResponseEntity<ApiResponse<OrderResponses.ShippingSaved>> updateShipping(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderRequests.Shipping request) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.updateShipping(principal.getUserId(), orderId, request)));
    }

    /** 거래 확정 — DELIVERED에서만 가능. 정산(PENDING)이 함께 생성된다 */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<OrderResponses.Confirmed>> confirm(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.confirm(principal.getUserId(), orderId)));
    }

    /** 주문 취소 — 출고 전까지만. 상품은 다시 판매중으로 복귀 */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponses.Cancelled>> cancel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderRequests.Cancel request) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.cancel(principal.getUserId(), principal.isAdmin(), orderId, request)));
    }
}
