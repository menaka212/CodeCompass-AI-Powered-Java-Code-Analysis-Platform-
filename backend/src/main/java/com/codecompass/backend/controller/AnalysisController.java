package com.codecompass.backend.controller;

import com.codecompass.backend.dto.RepositoryAnalysisRequest;
import com.codecompass.backend.service.CodeAnalysisService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final CodeAnalysisService codeAnalysisService;

    public AnalysisController(
            CodeAnalysisService codeAnalysisService
    ) {
        this.codeAnalysisService = codeAnalysisService;
    }


    @PostMapping("/explain")
    public String explainCode(
            @RequestBody String content
    ) {
        return codeAnalysisService
                .generateCodeExplanation(content);
    }


    @PostMapping("/repository")
    public String analyzeRepository(
            @Valid @RequestBody RepositoryAnalysisRequest request
    ) {

        return codeAnalysisService
                .generateRepositoryAnalysis(
                        request.getGithubUrl()
                );
    }
}