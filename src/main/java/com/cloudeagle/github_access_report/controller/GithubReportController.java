package com.cloudeagle.github_access_report.controller;

import com.cloudeagle.github_access_report.model.AccessReport;
import com.cloudeagle.github_access_report.service.GithubAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class GithubReportController {

    private final GithubAccessService service;

    public GithubReportController(GithubAccessService service) {
        this.service = service;
    }

    @GetMapping("/access-report")
    public ResponseEntity<AccessReport> getAccessReport(@RequestParam String org) {
        if (org == null || org.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization name is required");
        }
        return ResponseEntity.ok(service.generateReport(org.trim()));
    }
}