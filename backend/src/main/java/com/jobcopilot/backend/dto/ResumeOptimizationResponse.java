package com.jobcopilot.backend.dto;

import java.util.List;

public class ResumeOptimizationResponse {

    private String optimizedResume;
    private List<String> changes;
    private int estimatedScoreImprovement;

    public ResumeOptimizationResponse() {}

    public String getOptimizedResume() { return optimizedResume; }
    public void setOptimizedResume(String optimizedResume) {
        this.optimizedResume = optimizedResume;
    }

    public List<String> getChanges() { return changes; }
    public void setChanges(List<String> changes) {
        this.changes = changes;
    }

    public int getEstimatedScoreImprovement() {
        return estimatedScoreImprovement;
    }
    public void setEstimatedScoreImprovement(int estimatedScoreImprovement) {
        this.estimatedScoreImprovement = estimatedScoreImprovement;
    }
}