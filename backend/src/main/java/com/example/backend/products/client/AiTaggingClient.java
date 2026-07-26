package com.example.backend.products.client;

import com.example.backend.products.dto.AiTaggingResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;

/**
 * FastAPI 추론 서버 호출 클라이언트.
 * <p>
 * 핵심은 <b>절대 예외를 밖으로 던지지 않는다</b>는 것이다.
 * 추론 서버가 죽어 있어도 판매자는 상품을 등록할 수 있어야 하므로,
 * 어떤 실패든 잡아서 available=false로 바꿔 돌려준다.
 */
@Slf4j
@Component
public class AiTaggingClient {

    private final RestClient restClient;

    public AiTaggingClient(
            @Value("${ai.server.base-url}") String baseUrl,
            @Value("${ai.server.connect-timeout-ms}") long connectTimeoutMs,
            @Value("${ai.server.read-timeout-ms}") long readTimeoutMs) {

        // 타임아웃을 반드시 걸어야 한다. 없으면 추론 서버가 멈췄을 때
        // 우리 서버의 요청 스레드가 무한정 붙잡혀서 같이 죽는다.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public AiTaggingResponse predict(MultipartFile image) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", toResource(image));

            Prediction result = restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Prediction.class);

            if (result == null) {
                return AiTaggingResponse.unavailable();
            }

            return AiTaggingResponse.success(
                    result.getCategory(), result.getColor(),
                    result.getStyle(), result.getGender(), result.getConfidence());

        } catch (Exception e) {
            // 연결 실패, 타임아웃, 5xx, JSON 파싱 실패 — 전부 여기로 떨어진다
            log.warn("AI 추론 서버 호출 실패 (fail-safe로 빈 제안값 반환): {}", e.getMessage());
            return AiTaggingResponse.unavailable();
        }
    }

    /** MultipartFile을 그대로 전송할 수 있게 파일명을 가진 Resource로 감싼다 */
    private ByteArrayResource toResource(MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }

    /** FastAPI 응답 매핑용 (Jackson이 setter로 채운다) */
    @Getter
    @Setter
    @NoArgsConstructor
    static class Prediction {
        private String category;
        private String color;
        private String style;
        private String gender;
        private BigDecimal confidence;
    }
}
