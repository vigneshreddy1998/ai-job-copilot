package com.jobcopilot.backend.dto;

public class AiResponse {
    private String response;

    public AiResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}