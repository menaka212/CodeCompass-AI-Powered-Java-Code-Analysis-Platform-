package com.codecompass.backend.controller;

import com.codecompass.backend.dto.RepositoryAiRequest;
import com.codecompass.backend.service.AiAnalysisService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiTestController {

    private final AiAnalysisService aiAnalysisService;

    public AiTestController(
            AiAnalysisService aiAnalysisService
    ) {
        this.aiAnalysisService = aiAnalysisService;
    }

    // Existing general AI endpoint
    @GetMapping("/test")
    public String testAi(
            @RequestParam String prompt
    ) {
        return aiAnalysisService.askAi(prompt);
    }

    // New repository-aware AI endpoint
    @PostMapping("/repository-ask")
    public String askRepositoryAi(
            @RequestBody RepositoryAiRequest request
    ) {
        return aiAnalysisService.askRepositoryAi(
                request.getGithubUrl(),
                request.getQuestion()
        );
    }
}