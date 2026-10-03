package com.codecompass.backend.dto;

public class ImpactCallerResponse {

    private String className;
    private String methodName;

    public ImpactCallerResponse(
            String className,
            String methodName
    ) {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }
}