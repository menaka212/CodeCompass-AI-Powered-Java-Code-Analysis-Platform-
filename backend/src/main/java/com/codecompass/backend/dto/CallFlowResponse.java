package com.codecompass.backend.dto;

import java.util.List;

public class CallFlowResponse {

    private String sourceClass;
    private String sourceMethod;
    private List<MethodCallResponse> calls;

    public CallFlowResponse(
            String sourceClass,
            String sourceMethod,
            List<MethodCallResponse> calls
    ) {
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;
        this.calls = calls;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public List<MethodCallResponse> getCalls() {
        return calls;
    }
}
