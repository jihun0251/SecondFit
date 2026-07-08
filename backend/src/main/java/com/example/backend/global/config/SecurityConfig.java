package com.example.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용 (위에서 만든 corsConfigurationSource Bean을 사용)
                .cors(cors -> {})
                // REST API라 CSRF 보호 불필요 (끄지 않으면 POST가 막힘)
                .csrf(csrf -> csrf.disable())
                // 어떤 요청에 인증이 필요한지 규칙 정의
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ⚠️ 지금은 전부 허용 (JWT 붙일 때 조일 것)
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트 주소 (Vite 개발 서버)
        config.setAllowedOrigins(List.of("http://localhost:5173",  "http://localhost:5174"));
        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 허용할 요청 헤더 (전부 허용)
        config.setAllowedHeaders(List.of("*"));
        // 인증정보(쿠키 등) 허용 — 나중에 JWT/쿠키 쓸 때 필요
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로(/**)에 이 규칙 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}