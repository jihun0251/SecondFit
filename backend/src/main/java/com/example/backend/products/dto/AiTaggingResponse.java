package com.example.backend.products.dto;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * AI 자동 태깅 응답.
 * <p>
 * ⚠️ Fail-safe 설계: 추론 서버가 죽어 있어도 200 + available=false로 응답한다.
 * AI는 "등록 폼 자동완성"이라는 편의 기능일 뿐인데, 여기서 500을 내면
 * 추론 서버 장애가 상품 등록 자체를 막아버리기 때문이다.
 */
@Getter
public class AiTaggingResponse {

    private final boolean available;
    private final String category;
    private final String color;
    private final String style;
    private final String gender;
    private final BigDecimal confidence;

    private AiTaggingResponse(boolean available, String category, String color,
                              String style, String gender, BigDecimal confidence) {
        this.available = available;
        this.category = category;
        this.color = color;
        this.style = style;
        this.gender = gender;
        this.confidence = confidence;
    }

    public static AiTaggingResponse success(String category, String color, String style,
                                            String gender, BigDecimal confidence) {
        return new AiTaggingResponse(true, category, color, style, gender, confidence);
    }

    /** 추론 서버 다운/타임아웃 시 */
    public static AiTaggingResponse unavailable() {
        return new AiTaggingResponse(false, null, null, null, null, null);
    }
}
