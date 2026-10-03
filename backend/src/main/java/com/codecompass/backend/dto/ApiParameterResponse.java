package com.codecompass.backend.dto;

public class ApiParameterResponse {

    private String name;
    private String type;
    private String source;

    public ApiParameterResponse(
            String name,
            String type,
            String source
    ) {
        this.name = name;
        this.type = type;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }
}