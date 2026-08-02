package com.jobcopilot.backend.service;

import com.jobcopilot.backend.dto.*;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class GapAnalysisService {

    private final AIService aiService;

    public GapAnalysisService(AIService aiService) {
        this.aiService = aiService;
    }

    public GapAnalysisResultDTO analyzeGap(
            JobAnalysisResponse job,
            ResumeAnalysisResponse resume,
            String rawJobDescription,
            String rawResumeText) {

        // ── 1. Extract data from DTOs ──────────────────────────────
        List<List<String>> skillGroups = job.getRequiredSkillGroups();
        List<String> preferredSkills = job.getPreferredSkills();
        List<String> resumeSkills = resume.getSkills();

        // Fallback: convert flat list to single-item groups
        if (skillGroups == null || skillGroups.isEmpty()) {
            skillGroups = new ArrayList<>();
            for (String skill : job.getRequiredSkills()) {
                skillGroups.add(List.of(skill));
            }
        }

        List<String> matchingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<String> toCheckWithAI = new ArrayList<>();
        List<List<String>> groupsToCheck = new ArrayList<>();

        // ── 2. Start holistic analysis in PARALLEL ─────────────────
        CompletableFuture<JobInsightsDTO> insightsFuture = CompletableFuture.supplyAsync(() -> aiService.analyzeHolistically(rawJobDescription, rawResumeText));

        // ── 3. Java exact match per GROUP ──────────────────────────
        for (List<String> group : skillGroups) {
            boolean groupMatched = group.stream()
                    .anyMatch(skill -> resumeSkills.stream()
                            .anyMatch(rs -> normalizeSkill(rs)
                                    .equalsIgnoreCase(normalizeSkill(skill))));

            if (groupMatched) {
                group.stream()
                        .filter(skill -> resumeSkills.stream()
                                .anyMatch(rs -> normalizeSkill(rs)
                                        .equalsIgnoreCase(normalizeSkill(skill))))
                        .findFirst()
                        .ifPresent(matchingSkills::add);
            } else {
                toCheckWithAI.addAll(group);
                groupsToCheck.add(group);
            }
        }

        // ── 4. AI semantic match for unmatched groups ──────────────
        if (!toCheckWithAI.isEmpty()) {
            SemanticMatchResult aiResult =
                    checkWithAI(toCheckWithAI, resumeSkills);

            for (List<String> group : groupsToCheck) {
                boolean groupMatchedByAI = aiResult.getDecisions()
                        .stream()
                        .filter(d -> group.contains(d.getRequired()))
                        .anyMatch(SkillMatchDecision::isMatched);

                if (groupMatchedByAI) {
                    aiResult.getDecisions().stream()
                            .filter(d -> group.contains(d.getRequired())
                                    && d.isMatched())
                            .findFirst()
                            .ifPresent(d -> matchingSkills.add(d.getRequired()));
                } else {
                    String missingLabel = group.size() > 1
                            ? String.join(" OR ", group)
                            : group.get(0);
                    missingSkills.add(missingLabel);
                    suggestions.add("Consider learning: " + missingLabel);
                }
            }
        }

        // ── 5. Calculate required score ────────────────────────────
        int requiredTotal = skillGroups.size();
        int requiredMatched = matchingSkills.size();
        int requiredScore = requiredTotal > 0
                ? (requiredMatched * 100) / requiredTotal
                : 100;

        // ── 6. Calculate preferred score ───────────────────────────
        int preferredTotal = 0;
        int preferredMatched = 0;

        if (preferredSkills != null && !preferredSkills.isEmpty()) {
            preferredTotal = preferredSkills.size();
            for (String preferred : preferredSkills) {
                boolean matched = resumeSkills.stream()
                        .anyMatch(rs -> normalizeSkill(rs)
                                .equalsIgnoreCase(normalizeSkill(preferred)));
                if (matched) preferredMatched++;
            }
        }

        int preferredScore = preferredTotal > 0
                ? (preferredMatched * 100) / preferredTotal
                : 100;

        // ── 7. Weighted final score with gate check ─────────────────
        int finalScore = requiredScore < 100
                ? requiredScore
                : 70 + (preferredScore * 30 / 100);

        // ── 8. Wait for holistic analysis ──────────────────────────
        JobInsightsDTO insights = null;
        try {
            insights = insightsFuture.get();
        } catch (Exception e) {
            System.err.println("Holistic analysis failed: " + e.getMessage());
        }

        // ── 9. Build and return result ──────────────────────────────
        GapAnalysisResultDTO result = new GapAnalysisResultDTO();
        result.setMatchScore(finalScore);
        result.setRequiredMatchScore(requiredScore);
        result.setPreferredMatchScore(preferredScore);
        result.setMatchingSkills(matchingSkills);
        result.setMissingSkills(missingSkills);
        result.setSuggestions(suggestions);
        result.setInsights(insights);

        return result;
    }

    private SemanticMatchResult checkWithAI(
            List<String> toCheck,
            List<String> resumeSkills) {

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

        Map<String, String> aliases = new HashMap<>();

        // Pure formatting differences only
        aliases.put("springboot", "spring boot");
        aliases.put("spring-boot", "spring boot");
        aliases.put("reactjs", "react");
        aliases.put("react js", "react");
        aliases.put("nodejs", "node.js");
        aliases.put("node js", "node.js");
        aliases.put("k8s", "kubernetes");
        aliases.put("postgres", "postgresql");
        aliases.put("js", "javascript");
        aliases.put("mongo", "mongodb");

        return aliases.getOrDefault(skill, skill);
    }
}