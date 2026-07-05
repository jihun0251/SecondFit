package com.example.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API라 CSRF 보호 불필요 (끄지 않으면 POST가 막힘)
                .csrf(csrf -> csrf.disable())
                // 어떤 요청에 인증이 필요한지 규칙 정의
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ⚠️ 지금은 전부 허용 (JWT 붙일 때 조일 것)
                );
        return http.build();
    }
}