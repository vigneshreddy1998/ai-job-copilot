package com.jobcopilot.backend.service;

import com.jobcopilot.backend.dto.JobAnalysisResponse;
import com.jobcopilot.backend.dto.ResumeAnalysisResponse;
import com.jobcopilot.backend.dto.ResumeOptimizationResponse;
import com.jobcopilot.backend.entity.JobAnalysisEntity;
import com.jobcopilot.backend.repository.JobAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final JobAnalysisRepository jobAnalysisRepository;
    private final GroqService groqService;

    public AIService(JobAnalysisRepository jobAnalysisRepository,
                     GroqService groqService) {
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.groqService = groqService;
    }

@Transactional
    public JobAnalysisResponse analyzeJob(String jobDescription) {

        RestTemplate restTemplate = new RestTemplate();

        String prompt = """
You are a strict JSON API.

Extract information from the job description.

RULES:
1. Return ONLY valid JSON
2. No explanations
3. No markdown
4. Do not infer technologies that are not mentioned
5. You MAY infer the role/title from context (e.g., if JD requires Java/Spring Boot/J2EE skills, role is "Java Developer" or "Java/J2EE Developer")
6. Use exact field names
7. Extract ALL skills mentioned, not just a few
8. requiredSkills are technologies explicitly required
9. preferredSkills are technologies mentioned as "preferred" or "nice to have"

JSON FORMAT:
{
  "role": "",
  "experience": "",
  "requiredSkills": [],
  "preferredSkills": []
}

Job Description:
""" + jobDescription;

    String aiResponse = groqService.callGroq(prompt);

    aiResponse = sanitizeResponse(aiResponse);

        System.out.println("Sanitized AI Response: " + aiResponse);

        try {
            ObjectMapper mapper = new ObjectMapper();

            JobAnalysisResponse parsedResponse =
                    mapper.readValue(aiResponse, JobAnalysisResponse.class);
            System.out.println("AI Raw Response: " + aiResponse);

            validateResponse(parsedResponse);

            JobAnalysisEntity entity = new JobAnalysisEntity();

            entity.setRole(parsedResponse.getRole());
            entity.setExperience(parsedResponse.getExperience());
            entity.setRequiredSkills(parsedResponse.getRequiredSkills());
            entity.setPreferredSkills(parsedResponse.getPreferredSkills());
            entity.setJobDescription(jobDescription);
            entity.setCreatedAt(LocalDateTime.now());
            System.out.println(entity.getRole());
            jobAnalysisRepository.save(entity);
            System.out.println("DATA SAVED SUCCESSFULLY");

            return parsedResponse;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response: " + aiResponse);
        }
    }
    private void validateResponse(JobAnalysisResponse response) {

        if (response.getRole() == null || response.getRole().isBlank()) {
            throw new RuntimeException("Role is missing");
        }

        if (response.getRequiredSkills() == null || response.getRequiredSkills().isEmpty()) {
            throw new RuntimeException("Required skills missing");
        }
    }
    private String sanitizeResponse(String response) {

        response = response.replace("```json", "");
        response = response.replace("```", "");

        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");

        if (start != -1 && end != -1) {
            return response.substring(start, end + 1);
        }

        return response;
    }
    public ResumeAnalysisResponse analyzeResume(String resumeText) {

        String prompt = """
You are a strict JSON API.

Extract ONLY explicitly mentioned information from the resume.

RULES:
1. Return ONLY valid JSON
2. No explanations
3. No markdown
4. Do not hallucinate information
5. Use exact field names
6. skills must be array of strings
7. projects must be array of strings, If no project is resume keep projects as null.
8. Do not use nested JSON objects
9. experience must contain only number of years in experience (eg: 3 years or 4 years)
10. skills must contain exact technologies mentioned
11. extract each skill separately
12. do not summarize technologies

JSON FORMAT:
{
  "name": "",
  "experience": "",
  "skills": [],
  "projects": []
}

Resume:
""" + resumeText;

        String aiResponse = groqService.callGroq(prompt);
        aiResponse = sanitizeResponse(aiResponse);

        aiResponse = sanitizeResponse(aiResponse);

        System.out.println("Resume AI Response: " + aiResponse);

        try {

            ObjectMapper mapper = new ObjectMapper();

            ResumeAnalysisResponse parsedResponse =
                    mapper.readValue(aiResponse, ResumeAnalysisResponse.class);

            return parsedResponse;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse resume response: " + aiResponse);
        }
    }
    public String simplePrompt(String prompt) {
        return groqService.callGroq(prompt);
    }
    public List<JobAnalysisEntity> getAllJobs() {

        return jobAnalysisRepository.findAll();
    }
    public ResumeOptimizationResponse optimizeResume(String jobDescription, String resumeText) {
      String prompt = """
You are an AI Resume Optimizer.

Compare the resume against the job description.

Suggest improvements to make the resume stronger for this role.

RULES:
1. Return ONLY valid JSON
2. No explanations
3. No markdown
4. suggestions must be array of strings

JSON FORMAT:
{
  "suggestions": []
}

JOB DESCRIPTION:
""" + jobDescription + """
RESUME:
""" + resumeText;

        String aiResponse = groqService.callGroq(prompt);
        aiResponse = sanitizeResponse(aiResponse);

        try {

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(
                    aiResponse,
                    ResumeOptimizationResponse.class);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to optimize resume: " + aiResponse);
        }
    }
    public String semanticGapMatch(String prompt) {
        String response = groqService.callGroq(prompt);
        return sanitizeResponse(response);
    }
}