package com.codecompass.backend.dto;

import java.util.List;

public class ControllerApiResponse {

    private String controllerName;
    private List<ApiEndpointResponse> endpoints;

    public ControllerApiResponse(
            String controllerName,
            List<ApiEndpointResponse> endpoints
    ) {
        this.controllerName = controllerName;
        this.endpoints = endpoints;
    }

    public String getControllerName() {
        return controllerName;
    }

    public List<ApiEndpointResponse> getEndpoints() {
        return endpoints;
    }
}