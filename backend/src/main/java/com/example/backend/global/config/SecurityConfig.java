package com.example.backend.global.config;

import com.example.backend.auth.jwt.JwtAccessDeniedHandler;
import com.example.backend.auth.jwt.JwtAuthenticationEntryPoint;
import com.example.backend.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용 (아래 corsConfigurationSource Bean을 사용)
                .cors(cors -> {
                })
                // REST API는 세션 쿠키로 인증하지 않으므로 CSRF 보호 불필요
                .csrf(csrf -> csrf.disable())
                // JWT 방식 = 서버가 세션을 들고 있지 않음 (무상태)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 기본 로그인 폼 / 브라우저 팝업 인증창 제거
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401
                        .accessDeniedHandler(jwtAccessDeniedHandler)           // 403
                )

                // ⚠️ 규칙은 위에서부터 순서대로 매칭되고 먼저 걸리는 규칙이 이긴다.
                //    좁은(구체적인) 규칙을 반드시 먼저 써야 한다.
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 업로드된 이미지 정적 서빙
                        .requestMatchers("/uploads/**").permitAll()

                        // 인증 불필요
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()

                        // /products/me 는 인증 필요 → /products/* 보다 반드시 먼저 선언
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/*").permitAll()

                        // 관리자 전용 (도메인 구현은 이후 단계)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 그 외 전부 인증 필요
                        .anyRequest().authenticated()
                )

                // 스프링 기본 인증 필터 자리 앞에 우리 JWT 필터를 끼워 넣는다
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트 주소 (Vite 개발 서버)
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174"));
        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 허용할 요청 헤더 (전부 허용)
        config.setAllowedHeaders(List.of("*"));
        // 프론트 JS가 읽어야 하는 응답 헤더 노출
        config.setExposedHeaders(List.of("Authorization"));
        // 인증정보(쿠키 등) 허용
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
