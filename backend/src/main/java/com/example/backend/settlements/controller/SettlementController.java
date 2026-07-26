package com.example.backend.settlements.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.global.common.PageResponse;
import com.example.backend.settlements.dto.SettlementDtos;
import com.example.backend.settlements.entity.Settlement;
import com.example.backend.settlements.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * 내 정산 내역.
     * ⚠️ /{settlementId} 매핑보다 위에 있어야 "me"가 ID로 해석되지 않는다.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<SettlementDtos.Summary>>> getMySettlements(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Settlement.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                settlementService.getMySettlements(principal.getUserId(), status, page, size)));
    }

    /** 정산 상세 — 판매자 본인 또는 ADMIN */
    @GetMapping("/{settlementId}")
    public ResponseEntity<ApiResponse<SettlementDtos.Detail>> getDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long settlementId) {

        return ResponseEntity.ok(ApiResponse.success(
                settlementService.getDetail(principal.getUserId(), principal.isAdmin(), settlementId)));
    }
}
