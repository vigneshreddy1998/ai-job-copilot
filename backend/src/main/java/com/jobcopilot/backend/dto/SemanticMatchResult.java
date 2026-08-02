package com.jobcopilot.backend.dto;

import java.util.List;

public class SemanticMatchResult {

    private List<SkillMatchDecision> decisions;

    public SemanticMatchResult() {
    }

    public List<SkillMatchDecision> getDecisions() {
        return decisions;
    }

    public void setDecisions(List<SkillMatchDecision> decisions) {
        this.decisions = decisions;
    }
}