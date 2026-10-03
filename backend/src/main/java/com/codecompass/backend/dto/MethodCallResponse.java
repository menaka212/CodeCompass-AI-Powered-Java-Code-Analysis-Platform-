package com.codecompass.backend.dto;

public class MethodCallResponse {

    private String sourceClass;
    private String sourceMethod;
    private String targetClass;
    private String targetMethod;

    public MethodCallResponse(
            String sourceClass,
            String sourceMethod,
            String targetClass,
            String targetMethod
    ) {
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public String getTargetMethod() {
        return targetMethod;
    }
}