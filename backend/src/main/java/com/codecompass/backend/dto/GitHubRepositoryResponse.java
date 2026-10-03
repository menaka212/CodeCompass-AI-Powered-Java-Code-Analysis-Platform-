package com.codecompass.backend.dto;

public class GitHubRepositoryResponse {

    private String name;
    private String description;
    private String language;
    private String defaultBranch;
    private String url;

    public GitHubRepositoryResponse(
            String name,
            String description,
            String language,
            String defaultBranch,
            String url
    ) {
        this.name = name;
        this.description = description;
        this.language = language;
        this.defaultBranch = defaultBranch;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public String getUrl() {
        return url;
    }
}