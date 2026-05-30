package com.jobcopilot.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner testGroqConfig(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.model}") String model) {
        return args -> {
            System.out.println("=== GROQ CONFIG TEST ===");
            System.out.println("Model: " + model);
            System.out.println("Key starts with: " + apiKey.substring(0, 7) + "...");
            System.out.println("Key length: " + apiKey.length());
            System.out.println("========================");
        };
    }
}