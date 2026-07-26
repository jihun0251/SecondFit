package com.example.backend.users.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.reviews.dto.ReviewDtos;
import com.example.backend.reviews.service.ReviewService;
import com.example.backend.users.dto.SignupRequest;
import com.example.backend.users.dto.SignupResponse;
import com.example.backend.users.dto.UserDtos;
import com.example.backend.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;

    /** 회원가입 — 201 Created */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request) {

        SignupResponse response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 내 프로필 조회.
     * ⚠️ /{userId} 매핑보다 위에 있어야 "me"가 userId로 해석되지 않는다.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDtos.MyProfile>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(principal.getUserId())));
    }

    /** 내 프로필 수정 */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserDtos.Updated>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserDtos.UpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                userService.updateMyProfile(principal.getUserId(), request)));
    }

    /** 회원 탈퇴 — status를 WITHDRAWN으로. 204 No Content */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal UserPrincipal principal) {
        userService.withdraw(principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    /** 특정 회원 공개 프로필 — 인증 불필요 */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDtos.PublicProfile>> getPublicProfile(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponse.success(userService.getPublicProfile(userId)));
    }

    /** 특정 회원이 받은 리뷰 목록 — 인증 불필요 */
    @GetMapping("/{userId}/reviews")
    public ResponseEntity<ApiResponse<ReviewDtos.SellerReviews>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getSellerReviews(userId, page, size)));
    }
}
