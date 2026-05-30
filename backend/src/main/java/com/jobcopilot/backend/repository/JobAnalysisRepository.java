package com.jobcopilot.backend.repository;

import com.jobcopilot.backend.entity.JobAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobAnalysisRepository extends JpaRepository<JobAnalysisEntity, Long> {
}