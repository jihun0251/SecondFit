package com.example.backend.auth.dto;

import com.example.backend.users.entity.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final UserInfo user;

    private LoginResponse(String accessToken, String refreshToken, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public static LoginResponse of(String accessToken, String refreshToken, User user) {
        return new LoginResponse(accessToken, refreshToken, UserInfo.from(user));
    }

    /** 로그인 직후 프론트가 헤더/마이페이지에 바로 뿌릴 최소 정보 */
    @Getter
    public static class UserInfo {
        private final Long userId;
        private final String nickname;
        private final User.Role role;

        private UserInfo(Long userId, String nickname, User.Role role) {
            this.userId = userId;
            this.nickname = nickname;
            this.role = role;
        }

        private static UserInfo from(User user) {
            return new UserInfo(user.getId(), user.getNickname(), user.getRole());
        }
    }
}
