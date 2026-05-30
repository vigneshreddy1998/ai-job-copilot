package com.jobcopilot.backend.controller;

import com.jobcopilot.backend.dto.*;
import com.jobcopilot.backend.entity.JobAnalysisEntity;
import com.jobcopilot.backend.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.jobcopilot.backend.service.GapAnalysisService;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("*")
public class AiController {
    @Autowired
    private GapAnalysisService gapAnalysisService;

    private final AIService AIService;

    public AiController(AIService AIService) {
        this.AIService = AIService;
    }

    @PostMapping("/analyze-job")
    public JobAnalysisResponse analyzeJob(@RequestBody JobAnalysisRequest request) {
        return AIService.analyzeJob(request.getJobDescription());
    }
    @PostMapping("/analyze-resume")
    public ResumeAnalysisResponse analyzeResume(
            @RequestBody ResumeAnalysisRequest request) {

        return AIService.analyzeResume(request.getResumeText());
    }
    @PostMapping("/analyze-gap")
    public GapAnalysisResultDTO analyzeGap(
            @RequestBody GapAnalysisRequest request) {

        return gapAnalysisService.analyzeGap(
                request.getJob(),
                request.getResume());
    }
    @GetMapping("/jobs")
    public List<JobAnalysisEntity> getAllJobs() {

        return AIService.getAllJobs();
    }
    @PostMapping("/optimize-resume")
    public ResumeOptimizationResponse optimizeResume(@RequestBody ResumeOptimizationRequest request) {
        return AIService.optimizeResume(request.getJobDescription(),  request.getResumeText());
    }
}