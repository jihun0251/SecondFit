package com.example.backend.auth.security;

import com.example.backend.users.entity.User;
import lombok.Getter;

/**
 * SecurityContext에 저장되는 "인증된 사용자" 정보.
 * 컨트롤러에서 @AuthenticationPrincipal UserPrincipal principal 로 꺼내 쓴다.
 * <p>
 * DB 조회 없이 JWT 안의 claim만으로 만들기 때문에 요청마다 쿼리가 나가지 않는다.
 */
@Getter
public class UserPrincipal {

    private final Long userId;
    private final String email;
    private final User.Role role;

    public UserPrincipal(Long userId, String email, User.Role role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }
}
