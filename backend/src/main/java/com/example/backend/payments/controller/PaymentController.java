package com.example.backend.payments.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.payments.dto.PaymentDtos;
import com.example.backend.payments.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** 모의 결제 처리 — 201 Created */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDtos.Created>> pay(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PaymentDtos.Request request) {

        PaymentDtos.Created response = paymentService.pay(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /** 결제 상세 — 본인 또는 ADMIN */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDtos.Detail>> getDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getDetail(principal.getUserId(), principal.isAdmin(), paymentId)));
    }
}
