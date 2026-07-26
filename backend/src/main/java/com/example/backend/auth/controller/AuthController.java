package com.example.backend.auth.controller;

import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.LoginResponse;
import com.example.backend.auth.service.AuthService;
import com.example.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 로그인 → accessToken / refreshToken 발급 */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    /**
     * 로그아웃.
     * <p>
     * ⚠️ 현재는 무상태(stateless) JWT라서 서버가 토큰을 폐기할 방법이 없다.
     * 실제 무효화는 프론트가 저장소(localStorage 등)에서 토큰을 지우는 것으로 이뤄진다.
     * 서버 측 강제 만료가 필요해지면 Redis 블랙리스트 또는
     * refresh_tokens 테이블을 추가하는 방식으로 확장하면 된다.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "logout success")));
    }
}
