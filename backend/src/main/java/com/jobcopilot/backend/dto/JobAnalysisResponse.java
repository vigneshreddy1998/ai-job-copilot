package com.jobcopilot.backend.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "role",
        "experience",
        "requiredSkills",
        "requiredSkillGroups",
        "preferredSkills"
})
public class JobAnalysisResponse {

    private String role;
    private String experience;
    private List<String> requiredSkills;
    private List<List<String>> requiredSkillGroups;
    private List<String> preferredSkills;

    public JobAnalysisResponse() {
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<List<String>> getRequiredSkillGroups() {
        return requiredSkillGroups;
    }
    public void setRequiredSkillGroups(List<List<String>> requiredSkillGroups) {
        this.requiredSkillGroups = requiredSkillGroups;
    }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) {
        this.preferredSkills = preferredSkills;
    }
}