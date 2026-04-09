package com.cloudeagle.github_access_report.client;

import com.cloudeagle.github_access_report.model.RepositoryAccess;
import com.cloudeagle.github_access_report.model.UserAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class GithubApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Value("${github.token}")
    private String githubToken;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        return headers;
    }

    public List<String> getAllRepositories(String org) {
        List<String> repos = new ArrayList<>();
        int page = 1;
        while (true) {
            String url = "https://api.github.com/orgs/" + org + "/repos?per_page=100&page=" + page;
            ResponseEntity<Object[]> response = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(createHeaders()), Object[].class);

            Object[] pageRepos = response.getBody();
            if (pageRepos == null || pageRepos.length == 0) break;

            for (Object repo : pageRepos) {
                Map<String, Object> map = (Map<String, Object>) repo;
                repos.add((String) map.get("name"));
            }
            page++;
        }
        return repos;
    }

    public List<UserAccess> buildAccessReport(String org) {
        List<String> repos = getAllRepositories(org);
        Map<String, List<RepositoryAccess>> userMap = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String repoName : repos) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                String url = "https://api.github.com/repos/" + org + "/" + repoName + "/collaborators?per_page=100";
                try {
                    ResponseEntity<Object[]> response = restTemplate.exchange(url, HttpMethod.GET,
                            new HttpEntity<>(createHeaders()), Object[].class);

                    Object[] collaborators = response.getBody();
                    if (collaborators != null) {
                        for (Object coll : collaborators) {
                            Map<String, Object> c = (Map<String, Object>) coll;
                            String username = (String) c.get("login");
                            @SuppressWarnings("unchecked")
                            Map<String, Boolean> perms = (Map<String, Boolean>) c.get("permissions");

                            String permission = determinePermission(perms);
                            userMap.computeIfAbsent(username, k -> new ArrayList<>())
                                    .add(new RepositoryAccess(repoName, permission));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching collaborators for " + repoName + ": " + e.getMessage());
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<UserAccess> users = new ArrayList<>();
        for (Map.Entry<String, List<RepositoryAccess>> entry : userMap.entrySet()) {
            users.add(new UserAccess(entry.getKey(), entry.getValue()));
        }
        return users;
    }

    private String determinePermission(Map<String, Boolean> perms) {
        if (perms == null) return "Read";
        if (Boolean.TRUE.equals(perms.get("admin"))) return "Admin";
        if (Boolean.TRUE.equals(perms.get("maintain"))) return "Maintain";
        if (Boolean.TRUE.equals(perms.get("push"))) return "Write";
        if (Boolean.TRUE.equals(perms.get("triage"))) return "Triage";
        return "Read";
    }
}