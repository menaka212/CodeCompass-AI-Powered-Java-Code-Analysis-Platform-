package com.codecompass.backend.dto;

import java.util.List;

public class DependencyGraphResponse {

    private String sourceClass;
    private List<String> dependencies;

    public DependencyGraphResponse(
            String sourceClass,
            List<String> dependencies
    ) {
        this.sourceClass = sourceClass;
        this.dependencies = dependencies;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}