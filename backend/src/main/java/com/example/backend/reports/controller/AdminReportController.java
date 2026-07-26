package com.example.backend.reports.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.global.common.PageResponse;
import com.example.backend.reports.dto.ReportDtos;
import com.example.backend.reports.entity.Report;
import com.example.backend.reports.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** 관리자 신고 처리 API */
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    /** 신고 목록 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReportDtos.Item>>> getReports(
            @RequestParam(required = false) Report.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(reportService.getReports(status, page, size)));
    }

    /** 신고 처리 — RESOLVED + SUSPEND_PRODUCT면 상품이 목록에서 사라진다 */
    @PatchMapping("/{reportId}")
    public ResponseEntity<ApiResponse<ReportDtos.Handled>> handle(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId,
            @Valid @RequestBody ReportDtos.HandleRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                reportService.handle(principal.getUserId(), reportId, request)));
    }
}
