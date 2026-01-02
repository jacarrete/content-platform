# Content Platform

A Spring Boot application to manage articles and search them using an AI assistant (Ollama).

This project demonstrates how to integrate AI capabilities safely into a traditional backend using feature flags, clean architecture principles, and modern Java features.

---

## Features

- Create and retrieve articles (CRUD).
- Paginated article listing.
- AI-powered semantic search using Ollama.
- Runtime feature flags to enable/disable AI without restarting.
- Safe fallback to keyword-based search when AI is disabled or unavailable.
- PostgreSQL persistence.
- Docker Compose setup for local development.

---

## Architecture Overview

The AI functionality is designed with safety and extensibility in mind.

- **AiContentRouter**  
  Central orchestration point.  
  Decides at runtime whether AI search is enabled.  
  Routes requests to the appropriate implementation.

- **AiContentService (sealed interface)**  
  Defines the AI search contract.  
  Explicitly restricts which implementations are allowed.

- **AI Providers**
    - OllamaContentService: Semantic search using an LLM.
    - NoOpAiContentService: Keyword-based search fallback.

This ensures:
- Clear separation of responsibilities.
- Safe AI rollout and rollback.
- No AI logic leaking into controllers.

---

## Runtime Feature Flags

AI search can be toggled at runtime, without redeploying the application.

### Toggle AI Search

Enable AI:
- bash: curl -X POST http://localhost:8080/features/ai-search/true

Disable AI:
- bash: curl -X POST http://localhost:8080/features/ai-search/false

Check status:
- bash: curl http://localhost:8080/features/ai-search

When disabled, the system automatically falls back to keyword-based search.

---

## REST API Endpoints

### 1. Create Article

- **URL:** /articles
- **Method:** POST
- **Body:**
    - json:
      ```json
      {
        "title": "Spring Boot Basics",
        "content": "Introduction to Spring Boot and its core concepts"
      }
      ```  
- **Response:** Created article with generated `id`.

---

### 2. Get Articles (Paginated)

- **URL:** /articles
- **Method:** GET
- **Query parameters:**
    - page (default: 0)
    - size (default: 10)

---

### 3. AI Search

- **URL:** /articles/search/ai
- **Method:** GET
- **Query parameter:** query

Example:
- bash: curl "http://localhost:8080/articles/search/ai?query=articles about spring ai"

---

## Running Locally with Docker Compose

### Prerequisites

- Docker
- Docker Compose
- At least 4 GB RAM recommended

### Start Services

- bash: docker compose up -d

This starts:
- PostgreSQL
- Ollama (AI server)

---

## Ollama Setup

Ollama requires a model to be pulled manually.

Pull the model:
- bash: docker exec -it ollama ollama pull moondream

Verify:
- bash: docker exec -it ollama ollama list

---

## Technology Stack

- Gradle 9.2.1
- Java 25
- Spring Boot 3.5.7
- Spring Data JPA
- Spring AI (Ollama)
- PostgreSQL
- Docker & Docker Compose

---

## Modern Java Features Used

- `record` for immutable DTOs.
- `sealed interface` to control allowed AI providers.
- Pattern matching and enhanced switch (where applicable).
- Text blocks for prompt definitions.
- Stream API improvements.
- `var` for local type inference.

---

## Design Goals

- Demonstrate safe AI adoption in backend systems.
- Keep AI behind feature flags.
- Avoid tight coupling between controllers and AI implementations.
- Use modern Java features where they add clarity and safety.

---

## Future Improvements

- Virtual threads for AI calls.
- Resilience (timeouts, circuit breakers).
- Metrics and observability for AI latency.
- Vector search with embeddings.
