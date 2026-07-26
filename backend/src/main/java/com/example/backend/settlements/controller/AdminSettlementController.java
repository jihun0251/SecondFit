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

/** 관리자 정산 처리 API */
@RestController
@RequestMapping("/api/v1/admin/settlements")
@RequiredArgsConstructor
public class AdminSettlementController {

    private final SettlementService settlementService;

    /**
     * 정산 목록 조회 (status: PENDING / COMPLETED).
     * ⚠️ 명세서에 없는 추가 엔드포인트 — 정산 처리할 건을 찾으려면 목록이 필요하다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SettlementDtos.AdminItem>>> getSettlements(
            @RequestParam(required = false) Settlement.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                settlementService.getSettlementsForAdmin(status, page, size)));
    }

    /** 정산 완료 처리 — PENDING → COMPLETED */
    @PostMapping("/{settlementId}/complete")
    public ResponseEntity<ApiResponse<SettlementDtos.Completed>> complete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long settlementId) {

        return ResponseEntity.ok(ApiResponse.success(
                settlementService.complete(principal.getUserId(), settlementId)));
    }
}
