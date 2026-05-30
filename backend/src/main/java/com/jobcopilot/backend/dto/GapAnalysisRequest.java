package com.jobcopilot.backend.dto;

public class GapAnalysisRequest {

    private JobAnalysisResponse job;

    private ResumeAnalysisResponse resume;

    public GapAnalysisRequest() {
    }

    public JobAnalysisResponse getJob() {
        return job;
    }

    public void setJob(JobAnalysisResponse job) {
        this.job = job;
    }

    public ResumeAnalysisResponse getResume() {
        return resume;
    }

    public void setResume(ResumeAnalysisResponse resume) {
        this.resume = resume;
    }
}