package com.jobcopilot.backend.dto;

public class GapAnalysisRequest {

    private JobAnalysisResponse job;
    private ResumeAnalysisResponse resume;
    private String rawJobDescription;
    private String rawResumeText;

    public GapAnalysisRequest() {}

    public JobAnalysisResponse getJob() { return job; }
    public void setJob(JobAnalysisResponse job) { this.job = job; }

    public ResumeAnalysisResponse getResume() { return resume; }
    public void setResume(ResumeAnalysisResponse resume) {
        this.resume = resume;
    }

    public String getRawJobDescription() { return rawJobDescription; }
    public void setRawJobDescription(String rawJobDescription) {
        this.rawJobDescription = rawJobDescription;
    }

    public String getRawResumeText() { return rawResumeText; }
    public void setRawResumeText(String rawResumeText) {
        this.rawResumeText = rawResumeText;
    }
}