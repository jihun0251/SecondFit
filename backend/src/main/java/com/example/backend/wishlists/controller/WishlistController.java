package com.example.backend.wishlists.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.wishlists.dto.WishlistDtos;
import com.example.backend.wishlists.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /** 내 찜 목록 — 가격 변동(priceChange) 포함 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistDtos.Item>>> getMyWishlists(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(ApiResponse.success(
                wishlistService.getMyWishlists(principal.getUserId())));
    }

    /** 찜 추가 — 201 Created */
    @PostMapping
    public ResponseEntity<ApiResponse<WishlistDtos.Created>> add(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WishlistDtos.Request request) {

        WishlistDtos.Created response = wishlistService.add(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /** 찜 취소 — 경로 변수가 wishlistId가 아니라 productId다. 204 No Content */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId) {

        wishlistService.remove(principal.getUserId(), productId);
        return ResponseEntity.noContent().build();
    }
}
