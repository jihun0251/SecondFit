package com.example.backend.settlements.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.settlements.dto.SettlementDtos;
import com.example.backend.settlements.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 정산 처리 API */
@RestController
@RequestMapping("/api/v1/admin/settlements")
@RequiredArgsConstructor
public class AdminSettlementController {

    private final SettlementService settlementService;

    /** 정산 완료 처리 — PENDING → COMPLETED */
    @PostMapping("/{settlementId}/complete")
    public ResponseEntity<ApiResponse<SettlementDtos.Completed>> complete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long settlementId) {

        return ResponseEntity.ok(ApiResponse.success(
                settlementService.complete(principal.getUserId(), settlementId)));
    }
}
