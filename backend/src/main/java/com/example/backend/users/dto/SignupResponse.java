package com.example.backend.users.dto;

import com.example.backend.users.entity.User;
import lombok.Getter;

@Getter
public class SignupResponse {

    private final Long userId;

    private SignupResponse(Long userId) {
        this.userId = userId;
    }

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId());
    }
}