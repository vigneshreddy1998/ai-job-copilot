package com.jobcopilot.backend.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({
        "matchScore",
        "matchingSkills",
        "missingSkills",
        "suggestions"
})
public class GapAnalysisResultDTO {

    private int matchScore;
    private int requiredMatchScore;
    private int preferredMatchScore;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private List<String> suggestions;
    private JobInsightsDTO insights;  // ← new field

    public GapAnalysisResultDTO() {}

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public int getRequiredMatchScore() { return requiredMatchScore; }
    public void setRequiredMatchScore(int requiredMatchScore) {
        this.requiredMatchScore = requiredMatchScore;
    }

    public int getPreferredMatchScore() { return preferredMatchScore; }
    public void setPreferredMatchScore(int preferredMatchScore) {
        this.preferredMatchScore = preferredMatchScore;
    }

    public List<String> getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(List<String> matchingSkills) {
        this.matchingSkills = matchingSkills;
    }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public JobInsightsDTO getInsights() { return insights; }
    public void setInsights(JobInsightsDTO insights) {
        this.insights = insights;
    }
}
