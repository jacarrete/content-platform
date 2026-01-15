# Content Platform

A Spring Boot application to manage articles and search them using an AI assistant (Ollama).

This project demonstrates how to integrate AI capabilities safely into a traditional backend using feature flags, clean architecture principles, modern Java features, and observability best practices.

---

## Features

* Create and retrieve articles (CRUD).
* Paginated article listing.
* AI-powered semantic search using Ollama.
* Runtime feature flags to enable/disable AI without restarting.
* Safe fallback to keyword-based search when AI is disabled or unavailable.
* PostgreSQL persistence.
* Observability: metrics, tracing, and structured logging.
* Docker Compose setup for local development.
* Redis caching for AI search results with TTL (5 minutes)

---

## Architecture Overview

The AI functionality is designed with **safety, observability, and extensibility** in mind.

* **AiContentRouter**
  Central orchestration point. Decides at runtime whether AI search is enabled and routes requests accordingly.
  Caches results in Redis for 5 minutes (TTL) to reduce repeated AI calls.

* **AiContentService (sealed interface)**
  Defines the AI search contract. Only allows explicit implementations.

* **AI Providers**

  * **OllamaContentService:** Semantic search using an LLM.
  * **NoOpAiContentService:** Keyword-based search fallback.

This ensures:

* Clear separation of responsibilities.
* Safe AI rollout and rollback.
* No AI logic leaking into controllers.

---

## Runtime Feature Flags

AI search can be toggled at runtime, without redeploying the application.

### Toggle AI Search

Enable AI:

```bash
curl -X POST http://localhost:8080/features/ai-search/true
```

Disable AI:

```bash
curl -X POST http://localhost:8080/features/ai-search/false
```

Check status:

```bash
curl http://localhost:8080/features/ai-search
```

When disabled, the system automatically falls back to keyword-based search.

---

## Observability

The system is fully observable using Micrometer and OpenTelemetry.

### Metrics:

AI search calls, query counts, article CRUD.

### Tracing:

Each AI search call is traced with a traceId and spanId.

### Logging:

Structured console logging with trace identifiers.

---

## REST API Endpoints

### 1. Create Article

* **URL:** /articles
* **Method:** POST
* **Body:**

  * json:

    ```json
    {
      "title": "Spring Boot Basics",
      "content": "Introduction to Spring Boot and its core concepts"
    }
    ```
* **Response:** Created article with generated `id`.

---

### 2. Get Articles (Paginated)

* **URL:** /articles
* **Method:** GET
* **Query parameters:**

  * page (default: 0)
  * size (default: 10)

---

### 3. AI Search

* **URL:** /articles/search/ai
* **Method:** GET
* **Query parameter:** query

Example:

```bash
curl "http://localhost:8080/articles/search/ai?query=articles about spring ai"
```

---

## Running Locally with Docker Compose

### Prerequisites

* Docker
* Docker Compose
* At least 4 GB RAM recommended

### Start Services

```bash
docker compose up -d
```

This starts:

* PostgreSQL
* Ollama (AI server)
* Kafka & Zookeeper

---

## Kafka Setup

The application uses Kafka for event streaming (e.g., article events). Docker Compose includes a Kafka broker and Zookeeper.

### Broker Configuration

* **Broker:** `kafka:9092`
* **Zookeeper:** `zookeeper:2181`
* **Topics:** Auto-created on first use (default).

### Notes on Networking

* The app container connects to Kafka using the hostname `kafka`. Ensure your Spring Boot `application.yml` Kafka config uses:

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:9092
```

* For local host access (e.g., Kafka UI), Kafka is exposed on `localhost:9092`.
* Optional: you can persist Kafka data by adding a Docker volume:

```yaml
volumes:
  kafka_data:
```

And in the `kafka` service:

```yaml
volumes:
  - kafka_data:/var/lib/kafka/data
```

This ensures topics and messages survive container restarts.

---

## Ollama Setup

Ollama requires a model to be pulled manually.

Pull the model:

```bash
docker exec -it ollama ollama pull moondream
```

Verify:

```bash
docker exec -it ollama ollama list
```

---

## Technology Stack

* Gradle 9.2.1
* Java 25
* Spring Boot 3.5.7
* Spring Data JPA
* Spring AI (Ollama)
* PostgreSQL
* Kafka (message broker)
* Kafka UI (for cluster monitoring)
* Docker & Docker Compose
* Micrometer & OpenTelemetry for observability
* Redis (caching for AI search)


---

## Modern Java Features Used

* `record` for immutable DTOs.
* `sealed interface` to control allowed AI providers.
* Pattern matching and enhanced switch (where applicable).
* Text blocks for prompt definitions.
* Stream API improvements.
* `var` for local type inference.

---

## Design Goals

* Demonstrate safe AI adoption in backend systems.
* Keep AI behind feature flags.
* Avoid tight coupling between controllers and AI implementations.
* Use modern Java features where they add clarity and safety.

---

## Future Improvements

* Virtual threads for AI calls.
* Resilience (timeouts, circuit breakers).
* Metrics and observability for AI latency.
* Vector search with embeddings.
