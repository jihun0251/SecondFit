package com.example.backend.auth.jwt;

import com.example.backend.global.common.ApiResponse;
import com.example.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증 자체가 안 된 요청(토큰 없음/만료/위조) → 401.
 * <p>
 * Security 필터 단계에서 터지는 예외는 @RestControllerAdvice가 못 잡는다.
 * (컨트롤러에 도달하기 전에 걸러지기 때문) → 여기서 직접 ApiResponse JSON을 써준다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ErrorCode ec = ErrorCode.UNAUTHORIZED;
        response.setStatus(ec.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(ec.getCode(), ec.getMessage()));
    }
}
