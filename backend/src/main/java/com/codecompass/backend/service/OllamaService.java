package com.codecompass.backend.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OllamaService {

    private final RestTemplate restTemplate;

    public OllamaService() {
        this.restTemplate = new RestTemplate();
    }

    public String generateResponse(String prompt) {

        String url = "http://localhost:11434/api/generate";

        Map<String, Object> request = Map.of(
                "model", "llama3.2",
                "prompt", prompt,
                "stream", false
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        url,
                        request,
                        Map.class
                );

        if (response.getBody() != null) {

            Object generatedResponse =
                    response.getBody().get("response");

            if (generatedResponse != null) {
                return generatedResponse.toString();
            }
        }

        return "No response generated.";
    }
}