package com.jobcopilot.backend.dto;

public class ResumeOptimizationRequest {

    private String jobDescription;
    private String resumeText;

    public ResumeOptimizationRequest() {}

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }
}