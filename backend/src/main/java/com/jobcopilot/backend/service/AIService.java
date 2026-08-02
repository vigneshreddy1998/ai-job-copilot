package com.jobcopilot.backend.service;

import com.jobcopilot.backend.dto.JobAnalysisResponse;
import com.jobcopilot.backend.dto.JobInsightsDTO;
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
4. Do not infer technologies not mentioned in the JD
5. You MAY infer the role/title from context
6. Use exact field names
7. Extract ALL skills mentioned, not just a few
8. requiredSkills: flat list of ALL required technologies
9. requiredSkillGroups: group skills connected by OR together
10. preferredSkills: technologies mentioned as "preferred" or "nice to have"
11. Each individual required skill gets its own single-item group
npm12. The following types go in requiredSkills ONLY,
NOT in requiredSkillGroups:
- Soft skills (communication, teamwork, leadership)
- General methodologies (Agile, SCRUM, SAFe, SDLC)
- General concepts (algorithms, concurrency, OOP,\s
design patterns, 12Factor)
- Architecture concepts (C4, DFDs, system design)
- Certifications (AWS certification, PMP)
- Domain experience (financial services, airline industry)
- Attitude/behavior (hardworking, attention to detail)

ONLY concrete technologies and tools go in\s
requiredSkillGroups:
- Programming languages (Java, Python, JavaScript)
- Frameworks (Spring Boot, React, Angular)
- Databases (MongoDB, PostgreSQL, Oracle)
- Cloud platforms (AWS, GCP, Azure)
- DevOps tools (Docker, Kubernetes, Jenkins)
- Specific APIs/protocols (GraphQL, REST, OAuth2)
- Specific tools (Git, JIRA, Kafka)

JSON FORMAT:
{
  "role": "",
  "experience": "",
  "requiredSkills": ["Java", "Python", "C++", "SQL", "Spring Boot", "MongoDB"],
  "requiredSkillGroups": [
    ["Java", "Python", "C++", "SQL"],
    ["Spring Boot"],
    ["MongoDB"]
  ],
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
    public ResumeOptimizationResponse optimizeResume(
            String jobDescription,
            String resumeText) {

        String prompt = """
You are an expert resume writer optimizing a resume for a specific job.

YOUR TASK:
Rewrite the resume to better match the job description while keeping 
ALL facts intact. NO HALLUCINATION ALLOWED.

CRITICAL RULES:
1. NEVER invent companies, dates, metrics, or experience
2. NEVER add skills the candidate does not have
3. ONLY rephrase, restructure, and emphasize existing content
4. Keep the candidate's voice and writing style
5. Return ONLY valid JSON, no markdown, no explanations

WHAT TO OPTIMIZE:
- Inject keywords from the JD naturally into existing bullets
- Strengthen weak bullets with stronger action verbs
- Quantify achievements where the original mentions metrics
- Remove or de-emphasize bullets irrelevant to THIS specific JD
- Match terminology from the JD (e.g., if JD says "REST APIs" 
  and resume says "RESTful services", change to "REST APIs")
- Reorder bullets to put JD-relevant ones first
- Keep section headings exactly as they were (SUMMARY, SKILLS, 
  EXPERIENCE, EDUCATION)
- Preserve all dates, companies, job titles, and degree info
- Keep the same overall length

WHAT TO CHANGE:
- Phrasing
- Action verbs
- Keyword density
- Bullet order within each role
- Emphasis on relevant achievements

WHAT NOT TO CHANGE:
- Companies, dates, locations
- Job titles
- Degrees, schools, graduation dates
- Names, contact info
- Real metrics and numbers

JSON FORMAT (return EXACTLY this structure):
{
  "optimizedResume": "<full rewritten resume as plain text with \\n for line breaks>",
  "changes": [
    "Replaced 'RESTful services' with 'REST APIs' to match JD terminology",
    "Strengthened bullet point about microservices with stronger action verbs",
    "Moved Kafka experience to top of Bank of America section",
    "Added emphasis on event-driven architecture to match JD focus"
  ],
  "estimatedScoreImprovement": 15
}

estimatedScoreImprovement should be a realistic integer 
between 5 and 25 representing percentage points improvement.

JOB DESCRIPTION:
%s

RESUME:
%s
""".formatted(jobDescription, resumeText);

        String aiResponse = groqService.callGroq(prompt);
        aiResponse = sanitizeResponse(aiResponse);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(
                    aiResponse,
                    ResumeOptimizationResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to optimize resume: " + aiResponse, e);
        }
    }
    public String semanticGapMatch(String prompt) {
        String response = groqService.callGroq(prompt);
        return sanitizeResponse(response);
    }
    public JobInsightsDTO analyzeHolistically(
            String jobDescription,
            String resumeText) {

        String prompt = """
    You are an expert technical recruiter evaluating a candidate.
    
    Analyze this resume against the job description carefully.
    Consider seniority level, experience years, industry background,
    and overall fit.
    
    RULES:
    1. Return ONLY valid JSON
    2. No markdown, no explanations
    3. Be specific — reference actual content from both documents
    4. keyStrengths and mainGaps must be arrays of strings
    5. overallFit must be one of: "Strong Fit", "Good Fit", 
       "Partial Fit", "Weak Fit"
    
    JSON FORMAT:
    {
      "seniorityMatch": "Candidate is mid-level (4 years), JD targets mid-senior (5+ years). Close match.",
      "experienceAssessment": "4 years Java experience vs 5+ required. Slight gap but strong project depth.",
      "industryRelevance": "Finance and retail background relevant for enterprise Java roles.",
      "keyStrengths": [
        "Strong Spring Boot and microservices experience",
        "Kafka event-driven architecture matches JD requirements",
        "AWS cloud native experience is a strong plus"
      ],
      "mainGaps": [
        "1 year short of preferred experience requirement",
        "No explicit mention of Spring Test Framework"
      ],
      "overallFit": "Good Fit",
      "recruiterPerspective": "Strong mid-level Java developer with relevant enterprise experience. Would likely pass technical screening."
    }
    
    JOB DESCRIPTION:
    %s
    
    RESUME:
    %s
    """.formatted(jobDescription, resumeText);

        String response = groqService.callGroq(prompt);
        response = sanitizeResponse(response);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response, JobInsightsDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse holistic analysis: " + response, e);
        }
    }
}