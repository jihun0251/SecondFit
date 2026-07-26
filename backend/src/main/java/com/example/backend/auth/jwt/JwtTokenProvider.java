package com.example.backend.auth.jwt;

import com.example.backend.users.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 생성 / 검증 담당.
 * <p>
 * 토큰 구조: header.payload.signature (Base64URL)
 * - subject : userId
 * - claim   : email, role, type(access|refresh)
 * - 서명    : HS256 (대칭키). 서버만 아는 secret으로 서명하므로 위조 불가.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    private final SecretKey key;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms}") long accessTokenValidityMs,
            @Value("${jwt.refresh-token-validity-ms}") long refreshTokenValidityMs) {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // HS256은 최소 256bit(32byte) 키를 요구한다. 짧으면 애플리케이션 부팅 시점에 바로 실패시킨다.
            throw new IllegalStateException("jwt.secret은 32바이트(256bit) 이상이어야 합니다. 현재: " + keyBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    /** 액세스 토큰: 실제 API 호출에 쓰는 짧은 수명 토큰 */
    public String createAccessToken(User user) {
        return build(user.getId(), user.getEmail(), user.getRole(), TYPE_ACCESS, accessTokenValidityMs);
    }

    /** 리프레시 토큰: 액세스 토큰 재발급용 긴 수명 토큰 (민감정보 claim 최소화) */
    public String createRefreshToken(User user) {
        return build(user.getId(), null, null, TYPE_REFRESH, refreshTokenValidityMs);
    }

    private String build(Long userId, String email, User.Role role, String type, long validityMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);

        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TYPE, type)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key);

        if (email != null) builder.claim(CLAIM_EMAIL, email);
        if (role != null) builder.claim(CLAIM_ROLE, role.name());

        return builder.compact();
    }

    /**
     * 서명·만료 검증 후 payload(claims) 반환.
     * 검증 실패 시 JwtException 계열이 던져진다.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 액세스 토큰으로 쓸 수 있는 유효한 토큰인지 (예외를 boolean으로 바꿔주는 래퍼) */
    public boolean isValidAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 토큰: {}", e.getMessage());
        }
        return false;
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public String getEmail(Claims claims) {
        return claims.get(CLAIM_EMAIL, String.class);
    }

    public User.Role getRole(Claims claims) {
        String role = claims.get(CLAIM_ROLE, String.class);
        return role == null ? User.Role.USER : User.Role.valueOf(role);
    }
}
