package com.naevigator.server.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClovaService {

    @Value("${naver.clova.api-url}")
    private String apiUrl;

    @Value("${naver.clova.api-key}")
    private String apiKey;

    @Value("${naver.clova.request-id}")
    private String requestId;

    public String generateIntroduction(String jobType, String keywords) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 바디 생성
        Map<String, Object> body = new HashMap<>();
        body.put("messages", new Object[]{
                Map.of("role", "system", "content", "당신은 자기소개서 생성 도우미입니다."),
                Map.of("role", "user", "content", "직무: " + jobType + ", 키워드: " + keywords + " 에 맞는 자기소개서를 작성해줘.")
        });
        body.put("maxTokens", 800);

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-CLOVASTUDIO-API-KEY", apiKey);
        headers.set("X-NCP-APIGW-API-KEY", apiKey);
        headers.set("X-NCP-CLOVASTUDIO-REQUEST-ID", requestId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // API 요청
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

        // 응답 파싱
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
            if (result != null && result.get("message") != null) {
                return result.get("message").toString();
            }
        }
        return "자기소개서 생성에 실패했습니다.";
    }
}
