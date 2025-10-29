// src/main/java/com/naevigator/server/ai/ClovaService.java
package com.naevigator.server.server.ai;

import com.naevigator.server.server.ai.dto.CoverLetterGenerateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
public class ClovaService {

    @Value("${naver.clova.api-url}")
    private String apiUrl;

    @Value("${naver.clova.api-key-id}")
    private String apiKeyId;   // (API Key ID)

    @Value("${naver.clova.secret-key}")
    private String secretKey;  // (Secret Key)

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateCoverLetter(CoverLetterGenerateRequest req) {
        String userPrompt = buildPrompt(req); // (프롬프트: LLM에게 어떻게 던질건지)

        // 요청 바디(JSON)
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messages", List.of(
                Map.of("role", "system", "content",
                        "너는 회사 공고와 질문에 맞춰 한국어 자기소개서를 작성하는 전문가야. 조건에 맞게 글을 작성해줘."),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("maxTokens", 800);     // (토큰: 모델 생성 글자 제한-회사에 글자제한 많음)
        body.put("temperature", 0.7);   // (창의성/변동성 조절)
        body.put("topP", 0.9);          // (누적확률 줄이기)

        // 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY-ID", apiKeyId);
        headers.set("X-NCP-APIGW-API-KEY", secretKey);
        headers.set("X-Request-Id", UUID.randomUUID().toString());      // (요청추적용)
        headers.set("X-Request-Timestamp", String.valueOf(Instant.now().toEpochMilli()));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> res = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);
            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                return "[생성 실패] 비정상 응답 코드: " + res.getStatusCode();
            }
            return extractText(res.getBody());
        } catch (RestClientException e) {
            return "[생성 실패] 외부 API 호출 오류: " + e.getMessage();
        }
    }

    private String buildPrompt(CoverLetterGenerateRequest req) {
        String tone = Optional.ofNullable(req.tone()).orElse("neutral");
        String length = Optional.ofNullable(req.length()).orElse("medium");
        String q = Optional.ofNullable(req.question()).orElse("(질문 미입력)");
        String kws = (req.keywords() == null || req.keywords().isEmpty())
                ? "(키워드 없음)"
                : String.join(", ", req.keywords());

        return """
               아래 정보를 바탕으로 한국어 자기소개서를 작성해줘.
               - 직무: %s
               - 질문: %s
               - 키워드(선택): %s
               - 톤: %s
               - 길이: %s
               
               요구사항:
               1) 질문에 직답 → 사례(숫자/성과) → 배운점/재현 계획 순서
               2) 키워드를 자연스럽게 녹일 것(어색한 나열 금지)
               3) 2~4문단, 중복/상투어/과장 최소화
               """.formatted(req.jobType(), q, kws, tone, length);
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map body) {
        Object result = body.get("result");
        if (result instanceof Map<?, ?> r) {
            Object msg = r.get("message");
            if (msg != null) return msg.toString();

            // 일부 응답은 outputText 같은 다른 키를 쓸 수도 있음
            Object output = r.get("outputText");
            if (output != null) return output.toString();
        }

        Object messages = body.get("messages");
        if (messages instanceof List<?> list && !list.isEmpty()) {
            Object last = list.get(list.size() - 1);
            if (last instanceof Map<?, ?> lm) {
                Object content = lm.get("content");
                if (content != null) return content.toString();
            }
        }

        return "[생성 실패] 응답에서 텍스트를 찾지 못했습니다.";
    }
}
