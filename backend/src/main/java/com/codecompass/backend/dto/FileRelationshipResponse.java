package com.codecompass.backend.dto;

import java.util.List;

public class FileRelationshipResponse {

    private String sourceClass;
    private List<String> dependencies;

    public FileRelationshipResponse(
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