package com.naevigator.server.server.ai;

import com.naevigator.server.server.ai.dto.CoverLetterGenerateRequest;
import com.naevigator.server.server.ai.dto.CoverLetterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public class CoverLetterController {
    private final ClovaService clovaService;

    public CoverLetterController(ClovaService clovaService) {
        this.clovaService = clovaService;
    }

    @PostMapping("/api/ai/cover-letter")
    public ResponseEntity<CoverLetterResponse> generateCoverLetter(@RequestBody CoverLetterGenerateRequest request) {
        String content = clovaService.generateCoverLetter(request);
        return ResponseEntity.ok(new CoverLetterResponse(content));
    }
}
