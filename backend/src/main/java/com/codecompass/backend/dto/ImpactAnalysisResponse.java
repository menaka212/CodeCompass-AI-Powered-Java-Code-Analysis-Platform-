package com.codecompass.backend.dto;

import java.util.List;

import com.codecompass.backend.enums.DatabaseOperation;

public class ImpactAnalysisResponse {

    private String targetClass;
    private String targetMethod;
    private ClassType classType;
    private DatabaseOperation databaseOperation;
    private List<ImpactCallerResponse> callers;

    public ImpactAnalysisResponse(
            String targetClass,
            String targetMethod,
            ClassType classType,
            DatabaseOperation databaseOperation,
            List<ImpactCallerResponse> callers
    ) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.classType = classType;
        this.databaseOperation = databaseOperation;
        this.callers = callers;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public ClassType getClassType() {
        return classType;
    }

    public DatabaseOperation getDatabaseOperation() {
        return databaseOperation;
    }

    public List<ImpactCallerResponse> getCallers() {
        return callers;
    }
}