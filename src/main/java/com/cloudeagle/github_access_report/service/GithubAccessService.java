package com.cloudeagle.github_access_report.service;

import com.cloudeagle.github_access_report.client.GithubApiClient;
import com.cloudeagle.github_access_report.model.AccessReport;
import com.cloudeagle.github_access_report.model.UserAccess;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GithubAccessService {

    private final GithubApiClient githubApiClient;

    public GithubAccessService(GithubApiClient githubApiClient) {
        this.githubApiClient = githubApiClient;
    }

    public AccessReport generateReport(String org) {
        List<UserAccess> users = githubApiClient.buildAccessReport(org);
        return new AccessReport(org, Instant.now(), users);
    }
}