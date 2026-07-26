package com.example.backend.auth.jwt;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.users.entity.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 모든 요청 앞단에서 Authorization 헤더를 까보고,
 * 유효한 JWT면 SecurityContext에 인증정보를 심어주는 필터.
 * <p>
 * 여기서 401을 직접 내지 않는다. 인증정보를 안 심으면
 * 뒤쪽 Security 인가 단계가 알아서 EntryPoint(401)로 넘긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.isValidAccessToken(token)) {
            Claims claims = jwtTokenProvider.parseClaims(token);

            Long userId = jwtTokenProvider.getUserId(claims);
            String email = jwtTokenProvider.getEmail(claims);
            User.Role role = jwtTokenProvider.getRole(claims);

            UserPrincipal principal = new UserPrincipal(userId, email, role);

            // Spring Security의 hasRole("ADMIN")은 내부적으로 "ROLE_ADMIN" 권한을 찾는다.
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /** "Authorization: Bearer xxx" 에서 xxx만 뽑아낸다 */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (StringUtils.hasText(header) && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length()).trim();
        }
        return null;
    }
}
