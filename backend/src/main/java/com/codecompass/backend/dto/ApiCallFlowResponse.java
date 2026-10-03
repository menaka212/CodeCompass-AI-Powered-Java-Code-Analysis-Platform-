package com.codecompass.backend.dto;

public class ApiCallFlowResponse {

    private String httpMethod;
    private String endpoint;
    private CallFlowNodeResponse callFlow;

    public ApiCallFlowResponse(
            String httpMethod,
            String endpoint,
            CallFlowNodeResponse callFlow
    ) {
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.callFlow = callFlow;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public CallFlowNodeResponse getCallFlow() {
        return callFlow;
    }
}