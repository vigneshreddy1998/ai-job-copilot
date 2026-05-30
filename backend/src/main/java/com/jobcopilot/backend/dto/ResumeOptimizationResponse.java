package com.jobcopilot.backend.dto;

import java.util.List;

public class ResumeOptimizationResponse {
    private List<String> suggestions;

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
