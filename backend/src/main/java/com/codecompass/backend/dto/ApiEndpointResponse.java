package com.codecompass.backend.dto;

import java.util.List;

public class ApiEndpointResponse {

    private String method;
    private String path;
    private String handler;
    private String returnType;
    private String responseStatus;
    private List<ApiParameterResponse> parameters;

    public ApiEndpointResponse(
            String method,
            String path,
            String handler,
            String returnType,
            String responseStatus,
            List<ApiParameterResponse> parameters
    ) {
        this.method = method;
        this.path = path;
        this.handler = handler;
        this.returnType = returnType;
        this.responseStatus = responseStatus;
        this.parameters = parameters;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHandler() {
        return handler;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<ApiParameterResponse> getParameters() {
        return parameters;
    }

    public String getResponseStatus() {
        return responseStatus;
    }
}