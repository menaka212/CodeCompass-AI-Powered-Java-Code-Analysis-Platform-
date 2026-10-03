package com.codecompass.backend.dto;

public class ClassDependencyResponse {

    private String sourceClass;
    private String targetClass;

    public ClassDependencyResponse(
            String sourceClass,
            String targetClass
    ) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getTargetClass() {
        return targetClass;
    }
}