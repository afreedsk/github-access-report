package com.cloudeagle.github_access_report.model;

import java.util.List;

public record UserAccess(String username, List<RepositoryAccess> repositories) {}