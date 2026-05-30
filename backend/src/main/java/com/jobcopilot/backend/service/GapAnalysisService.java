package com.jobcopilot.backend.service;

import com.jobcopilot.backend.dto.*;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GapAnalysisService {

    private final AIService aiService;


    public GapAnalysisService(AIService aiService) {
        this.aiService = aiService;
    }

    public GapAnalysisResultDTO analyzeGap(JobAnalysisResponse job, ResumeAnalysisResponse resume) {

        List<String> requiredSkills = job.getRequiredSkills();
        List<String> resumeSkills = resume.getSkills();

        // Step 1: Java exact match
        List<String> matchedExact = new ArrayList<>();
        List<String> toCheckWithAI = new ArrayList<>();

        for (String required : requiredSkills) {
            boolean exactMatch = resumeSkills.stream()
                    .anyMatch(rs -> normalizeSkill(rs)
                            .equalsIgnoreCase(normalizeSkill(required)));

            if (exactMatch) {
                matchedExact.add(required);
            } else {
                toCheckWithAI.add(required);
            }
        }

        // Step 2: Build result lists, start with exact matches
        List<String> matchingSkills = new ArrayList<>(matchedExact);
        List<String> missingSkills = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // Step 3: If there are unmatched skills, do ONE AI call
        if (!toCheckWithAI.isEmpty()) {
            SemanticMatchResult aiResult =
                    checkWithAI(toCheckWithAI, resumeSkills);

            for (SkillMatchDecision decision : aiResult.getDecisions()) {
                if (decision.isMatched()) {
                    matchingSkills.add(decision.getRequired());
                } else {
                    missingSkills.add(decision.getRequired());
                    suggestions.add("Consider learning or adding "
                            + decision.getRequired());
                }
            }
        }

        // Step 4: Calculate score
        int matchScore = 0;
        if (!requiredSkills.isEmpty()) {
            matchScore = (matchingSkills.size() * 100) / requiredSkills.size();
        }

        // Step 5: Build response
        GapAnalysisResultDTO result = new GapAnalysisResultDTO();
        result.setMatchScore(matchScore);
        result.setMatchingSkills(matchingSkills);
        result.setMissingSkills(missingSkills);
        result.setSuggestions(suggestions);

        return result;
    }

    private SemanticMatchResult checkWithAI(List<String> toCheck, List<String> resumeSkills) {

        String prompt = """
        You are a technical skill matcher. Return JSON only.

        For each REQUIRED skill below, decide if the CANDIDATE has it — 
        either directly OR through a closely related skill 
        (like Spring Boot matching "Spring").

        REQUIRED SKILLS:
        %s

        CANDIDATE SKILLS:
        %s

        Return JSON in this exact format:
        {
          "decisions": [
            {
              "required": "<skill from required list>",
              "matched": true,
              "via": "<which candidate skill matched it, or empty string>"
            }
          ]
        }

        Rules:
        - Be strict. Don't match unrelated skills.
        - "Spring" matches "Spring Boot" (related framework family)
        - "Java" does NOT match "JavaScript" (completely different)
        - Vague concepts (Testing, SDLC) match if candidate has 
          concrete tools (JUnit, Mockito imply Testing knowledge)
        - Return exactly one decision per required skill
        - Use empty string "" for via when matched is false
        """.formatted(
                String.join(", ", toCheck),
                String.join(", ", resumeSkills)
        );

        String aiResponse = aiService.semanticGapMatch(prompt);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(aiResponse, SemanticMatchResult.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse AI semantic match: " + aiResponse, e);
        }
    }

    private String normalizeSkill(String skill) {
        skill = skill.toLowerCase().trim();

        Map<String, String> aliases = Map.of(
                "react js", "react",
                "reactjs", "react",
                "js", "javascript",         // ✅ FIXED: abbrev → full name
                "nodejs", "node.js",        // ✅ FIXED: keep full name as standard
                "node js", "node.js",
                "spring framework", "spring boot",
                "k8s", "kubernetes",
                "postgres", "postgresql"
        );

        return aliases.getOrDefault(skill, skill);
    }
}