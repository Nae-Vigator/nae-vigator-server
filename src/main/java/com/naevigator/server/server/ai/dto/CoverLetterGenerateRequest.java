package com.naevigator.server.server.ai.dto;

import java.util.List;

public record CoverLetterGenerateRequest(
        String jobType,            // 직무(예: "백엔드")
        List<String> keywords,     // 키워드 목록
        String tone,               // 톤(예: neutral/formal/cheerful)
        String length,             // 길이(예: short/medium/long)
        String question            // 회사 자기소개서 문항(선택)
) {}
