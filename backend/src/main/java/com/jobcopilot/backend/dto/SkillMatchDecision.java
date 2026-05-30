package com.jobcopilot.backend.dto;

public class SkillMatchDecision {

    private String required;
    private boolean matched;
    private String via;

    public SkillMatchDecision() {
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }
}