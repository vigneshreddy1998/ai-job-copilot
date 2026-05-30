package com.jobcopilot.backend.dto;

import java.util.List;

public class ResumeOptimizationRequest {
    private String jobDescription;
    private String resumeText;

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
