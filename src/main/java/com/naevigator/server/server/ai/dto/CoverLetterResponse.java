package com.naevigator.server.server.ai.dto;

public class CoverLetterResponse {
    private String content;

    public CoverLetterResponse() {}                // Jackson 역직렬화용 기본 생성자
    public CoverLetterResponse(String content) {   // 편의 생성자
        this.content = content;
    }

    public String getContent() {                   // getter
        return content;
    }

    public void setContent(String content) {       // setter (필요시)
        this.content = content;
    }
}