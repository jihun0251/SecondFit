package com.example.backend.inbounds.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.global.common.PageResponse;
import com.example.backend.inbounds.dto.InboundConfirmResponse;
import com.example.backend.inbounds.dto.InboundSummaryResponse;
import com.example.backend.inbounds.entity.Inbound;
import com.example.backend.inbounds.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자(본사) 입고 API.
 * /api/v1/admin/** 는 SecurityConfig에서 hasRole("ADMIN")으로 막혀 있다.
 */
@RestController
@RequestMapping("/api/v1/admin/inbounds")
@RequiredArgsConstructor
public class AdminInboundController {

    private final InboundService inboundService;

    /** 입고 목록 조회 (status: AWAITING / CONFIRMED) */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InboundSummaryResponse>>> getInbounds(
            @RequestParam(required = false) Inbound.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(inboundService.getInbounds(status, page, size)));
    }

    /** 입고 확인 → 상품이 즉시 판매중으로 전환된다 (검수 없는 v3 모델) */
    @PostMapping("/{inboundId}/confirm")
    public ResponseEntity<ApiResponse<InboundConfirmResponse>> confirm(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long inboundId) {

        return ResponseEntity.ok(ApiResponse.success(
                inboundService.confirm(principal.getUserId(), inboundId)));
    }
}
