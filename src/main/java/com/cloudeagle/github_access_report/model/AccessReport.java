package com.cloudeagle.github_access_report.model;

import java.time.Instant;
import java.util.List;

public record AccessReport(
        String organization,
        Instant generatedAt,
        List<UserAccess> users
) {}