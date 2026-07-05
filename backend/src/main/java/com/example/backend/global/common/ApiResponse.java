package com.example.backend.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON에서 숨김
public class ApiResponse<T> {

    private final boolean success;
    private final T data;      // 성공 시 실제 데이터
    private final ErrorBody error; // 실패 시 에러 정보

    private ApiResponse(boolean success, T data, ErrorBody error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    // 성공 응답 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // 성공 응답 (데이터 없음 - 삭제 등)
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }

    // 실패 응답
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }

    @Getter
    public static class ErrorBody {
        private final String code;
        private final String message;

        public ErrorBody(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}