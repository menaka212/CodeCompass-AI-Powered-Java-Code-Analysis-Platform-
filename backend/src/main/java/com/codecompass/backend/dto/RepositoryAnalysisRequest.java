package com.codecompass.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class RepositoryAnalysisRequest {

    @NotBlank(message = "GitHub URL is required")
    private String githubUrl;

    public RepositoryAnalysisRequest() {
    }

    public RepositoryAnalysisRequest(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }
}