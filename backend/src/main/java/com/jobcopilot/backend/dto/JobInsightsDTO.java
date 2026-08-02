package com.jobcopilot.backend.dto;

import java.util.List;

public class JobInsightsDTO {

    private String seniorityMatch;
    private String experienceAssessment;
    private String industryRelevance;
    private List<String> keyStrengths;
    private List<String> mainGaps;
    private String overallFit;
    private String recruiterPerspective;

    public JobInsightsDTO() {}

    public String getSeniorityMatch() { return seniorityMatch; }
    public void setSeniorityMatch(String seniorityMatch) {
        this.seniorityMatch = seniorityMatch;
    }

    public String getExperienceAssessment() { return experienceAssessment; }
    public void setExperienceAssessment(String experienceAssessment) {
        this.experienceAssessment = experienceAssessment;
    }

    public String getIndustryRelevance() { return industryRelevance; }
    public void setIndustryRelevance(String industryRelevance) {
        this.industryRelevance = industryRelevance;
    }

    public List<String> getKeyStrengths() { return keyStrengths; }
    public void setKeyStrengths(List<String> keyStrengths) {
        this.keyStrengths = keyStrengths;
    }

    public List<String> getMainGaps() { return mainGaps; }
    public void setMainGaps(List<String> mainGaps) {
        this.mainGaps = mainGaps;
    }

    public String getOverallFit() { return overallFit; }
    public void setOverallFit(String overallFit) {
        this.overallFit = overallFit;
    }

    public String getRecruiterPerspective() { return recruiterPerspective; }
    public void setRecruiterPerspective(String recruiterPerspective) {
        this.recruiterPerspective = recruiterPerspective;
    }
}