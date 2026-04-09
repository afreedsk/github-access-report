# GitHub Organization Access Report Service

A Spring Boot REST service that generates a report showing which users have access to which repositories in a given GitHub organization.

## Features
- Secure authentication with GitHub Personal Access Token
- Fetches all repositories with pagination
- Fetches collaborators in parallel for better performance
- Aggregates data: User → List of Repositories + Permission
- Clean JSON API response
- Handles 100+ repositories and 1000+ users efficiently

## Tech Stack
- Java 23
- Spring Boot 3.3+
- RestTemplate + CompletableFuture
- Lombok (optional)

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/afreedsk/github-access-report.git
