package com.codecompass.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ArchitectureExplanationRequest {

    @NotBlank
    private String githubUrl;

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }
}