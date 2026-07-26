package com.example.backend.reviews.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.reviews.dto.ReviewDtos;
import com.example.backend.reviews.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 리뷰 작성 — 거래 확정(CONFIRMED)된 본인 주문에만 가능. 201 Created */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDtos.Created>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReviewDtos.Request request) {

        ReviewDtos.Created response = reviewService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /** 리뷰 삭제 — 작성자 본인만. 204 No Content */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reviewId) {

        reviewService.delete(principal.getUserId(), reviewId);
        return ResponseEntity.noContent().build();
    }
}
