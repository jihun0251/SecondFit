package com.example.backend.global.exception;

import com.example.backend.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 우리가 의도적으로 던진 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode ec = e.getErrorCode();
        log.warn("BusinessException: {} - {}", ec.getCode(), ec.getMessage());
        return ResponseEntity
                .status(ec.getStatus())
                .body(ApiResponse.error(ec.getCode(), ec.getMessage()));
    }

    // @Valid 검증 실패 (DTO 유효성)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("유효성 검증에 실패했습니다.");
        return badRequest(msg);
    }

    /**
     * 쿼리 파라미터 타입 불일치.
     * 예: ?conditionGrade=BEST 처럼 enum에 없는 값 → 그냥 두면 500이 나간다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return badRequest("파라미터 '" + e.getName() + "'의 값이 올바르지 않습니다: " + e.getValue());
    }

    /** 필수 쿼리 파라미터 누락 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return badRequest("필수 파라미터가 누락되었습니다: " + e.getParameterName());
    }

    /** JSON 자체가 깨졌거나 타입이 안 맞아 역직렬화 실패 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("요청 본문 파싱 실패: {}", e.getMessage());
        return badRequest("요청 본문(JSON) 형식이 올바르지 않습니다.");
    }

    // 그 외 예상 못한 모든 예외 (최후의 보루)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e); // 스택트레이스 로그로
        ErrorCode ec = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity
                .status(ec.getStatus())
                .body(ApiResponse.error(ec.getCode(), ec.getMessage()));
    }

    private ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        ErrorCode ec = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(ec.getStatus()).body(ApiResponse.error(ec.getCode(), message));
    }
}
