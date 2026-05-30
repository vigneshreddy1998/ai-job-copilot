package com.jobcopilot.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    public String callGroq(String prompt) {

        // Step 1: Set up HTTP client
        RestTemplate restTemplate = new RestTemplate();

        // Step 2: Build headers (Content-Type + Bearer auth)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Step 3: Build the request body
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.1);
        body.put("response_format", Map.of("type", "json_object"));

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        body.put("messages", List.of(message));

        // Step 4: Combine headers + body into HttpEntity
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // Step 5: Make the API call
        Map response = restTemplate.postForObject(apiUrl, request, Map.class);

        // Step 6: Navigate Groq's nested response structure
        List<Map> choices = (List<Map>) response.get("choices");
        Map firstChoice = choices.get(0);
        Map messageObj = (Map) firstChoice.get("message");

        return messageObj.get("content").toString();
    }

}