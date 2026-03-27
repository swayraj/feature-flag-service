# Canary — Feature Flag Service

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-Cache-red?style=flat-square&logo=redis)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker)
![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)

> A production-grade feature flag engine — gradual rollouts, real-time broadcasts, webhook delivery, and Redis-cached evaluations. Built API-first and Kubernetes-ready.

**[Live Demo](https://feature-flag-service-production-7deb.up.railway.app)** · **[API Docs (Swagger)](https://feature-flag-service-production-7deb.up.railway.app/swagger-ui/index.html)**

---

## What is this?

Feature flags let you ship code to production without activating it — you flip a switch to turn a feature on or off without redeploying. At scale, that means gradual rollouts (5% of users → 25% → 100%), instant kill switches, and targeting specific users or segments.

Canary is a self-hosted feature flag service that handles all of this. It evaluates flags in microseconds using consistent hashing (same user always gets the same result), caches evaluations in Redis, and pushes every flag change to connected clients over WebSocket in real time.

---

## Features

**Flag Management**
- Full CRUD API for flags with name validation
- Toggle flags on/off instantly
- Search and filter flags

**Rollout Engine**
- Percentage-based gradual rollouts (0–100%) using consistent hashing — deterministic per user
- User targeting lists — force specific users into a flag regardless of rollout %
- Attribute-based segmentation — target by `country`, `platform`, or any custom attribute
- Batch evaluation — evaluate a flag for thousands of users in one request
- Scheduled rollouts — one-time activation at a future datetime or auto-gradual (step % every N hours)

**Infrastructure**
- Redis cache with 10-minute TTL and automatic invalidation on every flag change
- WebSocket (STOMP) real-time broadcast — every flag event pushed to all connected clients instantly
- Webhook delivery — async, 3-retry with exponential backoff, HMAC-SHA256 request signing
- API key authentication on all write endpoints
- Rate limiting

**Observability & Docs**
- Swagger/OpenAPI UI at `/swagger-ui/index.html`
- Spring Boot Actuator health probes (`/actuator/health`)
- Distribution analysis — simulate any flag against N users, see bucket spread

**Admin UI**
- React + Tailwind dashboard — create, edit, delete, toggle flags
- Evaluation Simulator — type any userId, see ENABLED/DISABLED, which bucket, why (% rule vs target list), cache hit vs DB fetch
- Live Evaluation Feed — real-time WebSocket stream of flag events
- Rollout Visualizer — 200-dot grid that recolors live as you drag the rollout % slider
- Story Panel — Redis hit/miss ratio, evaluation counts per flag, recent decisions timeline
- Webhook Panel — register endpoints, view per-delivery logs with HTTP status and payload preview
- Scheduling UI — schedule one-time or gradual rollouts per flag

---

## Architecture

```mermaid
graph TD
    subgraph Client
        UI[React Admin UI]
        SDK[API Consumer / SDK]
    end

    subgraph API Layer
        FC[FlagController<br/>/api/flags]
        EC[EvaluationController<br/>/api/evaluate]
        WH[WebhookController<br/>/api/webhooks]
        SC[SchedulingController<br/>/api/schedule]
    end

    subgraph Service Layer
        FS[FlagService]
        RS[RolloutService<br/>consistent hash]
        FES[FlagEventService<br/>event bus]
        SS[SchedulingService<br/>@Scheduled]
    end

    subgraph Data
        PG[(PostgreSQL<br/>flags table)]
        RD[(Redis<br/>10-min TTL cache)]
    end

    subgraph Broadcast
        WS[WebSocket / STOMP<br/>/ws]
        WHK[Webhook Delivery<br/>HMAC-SHA256 + retry]
    end

    UI -->|REST + WebSocket| FC
    UI -->|REST + WebSocket| EC
    SDK -->|REST| EC

    FC --> FS
    EC --> RS
    RS -->|cache lookup| RD
    RS -->|cache miss| PG
    FS -->|write| PG
    FS -->|evict| RD
    FS --> FES
    FES -->|broadcast| WS
    FES -->|async deliver| WHK
    SS --> FS
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0 |
| Database | PostgreSQL 16 |
| Cache | Redis (Spring Cache, 10-min TTL) |
| Real-time | WebSocket + STOMP (SockJS) |
| Webhooks | Spring `@Async` + RestTemplate + HMAC-SHA256 |
| Auth | API key filter (`X-API-Key` header) |
| Scheduling | Spring `@Scheduled` |
| Testing | JUnit 5, Mockito, TestContainers |
| API Docs | springdoc-openapi 3.0.2 |
| Container | Docker (multi-stage build) |
| Orchestration | Kubernetes (manifests in `k8s/`) |
| CI/CD | GitHub Actions |
| Frontend | React 18 + Vite 5 + Tailwind CSS 4 |
| Hosting | Railway |

---

## Getting Started

### Prerequisites
- Docker and Docker Compose

### Run locally

```bash
git clone https://github.com/swayraj/feature-flag-service.git
cd feature-flag-service
docker-compose up --build
```

This starts three containers: the Spring Boot app, PostgreSQL, and Redis.

| Endpoint | URL |
|---|---|
| API | http://localhost:8080 |
| Admin UI | http://localhost:8080/app |
| Swagger | http://localhost:8080/swagger-ui/index.html |
| Health | http://localhost:8080/actuator/health |

Write endpoints require the API key header:
```
X-API-Key: canary-secret-key
```

### Run without Docker

```bash
# Start Postgres and Redis separately, then:
./mvnw spring-boot:run
```

---

## API Reference

### Flags

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/flags` | — | List all flags |
| GET | `/api/flags/{id}` | — | Get flag by ID |
| POST | `/api/flags` | required | Create flag |
| PUT | `/api/flags/{id}` | required | Update flag |
| DELETE | `/api/flags/{id}` | required | Delete flag |
| POST | `/api/flags/{id}/toggle` | required | Toggle on/off |
| GET | `/api/flags/search?name=` | — | Search by name |

### Evaluation

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/evaluate` | — | Evaluate flag for a user |
| GET | `/api/evaluate/{flagName}?userId=` | — | Simple GET evaluation |
| GET | `/api/evaluate/user/{userId}` | — | Evaluate all flags for a user |
| POST | `/api/evaluate/batch` | — | Evaluate flag for many users |
| POST | `/api/evaluate/segment` | — | Evaluate with user attributes |
| GET | `/api/evaluate/{flagName}/stats` | — | Rollout statistics |
| GET | `/api/evaluate/{flagName}/simulate?numberOfUsers=` | — | Simulate rollout |

### Examples

**Evaluate a flag for a user**
```bash
curl -X POST http://localhost:8080/api/evaluate \
  -H "Content-Type: application/json" \
  -d '{"flagName": "dark_mode", "userId": "user-42"}'
```
```json
{
  "flagName": "dark_mode",
  "userId": "user-42",
  "enabled": true,
  "bucket": 37,
  "reason": "ROLLOUT_PERCENTAGE"
}
```

**Evaluate with attributes (segmentation)**
```bash
curl -X POST http://localhost:8080/api/evaluate/segment \
  -H "Content-Type: application/json" \
  -d '{"flagName": "new_checkout", "userId": "user-7", "attributes": {"country": "US", "platform": "iOS"}}'
```

**Create a flag**
```bash
curl -X POST http://localhost:8080/api/flags \
  -H "Content-Type: application/json" \
  -H "X-API-Key: canary-secret-key" \
  -d '{"name": "dark_mode", "enabled": false, "rolloutPercentage": 25}'
```

---

## Tech Decisions

**Consistent hashing for rollout evaluation** — `hash(flagName + userId) % 100` means the same user always lands in the same bucket. Without this, a user at 50% rollout would flip between enabled/disabled on every request — a terrible experience. Determinism is non-negotiable for feature flags.

**Redis over in-memory cache** — An in-memory cache works fine on one instance but breaks the moment you scale horizontally. Redis is shared across all instances, survives restarts, and keeps evaluation latency consistent regardless of which pod handles the request.

**Cache eviction on every write** — When a flag is updated or toggled, the cache entry is evicted immediately. This trades a slightly higher DB hit rate (one extra read per update) for correctness — stale flag state is a silent bug that's very hard to debug in production.

**Async webhook delivery** — Webhook calls go through `@Async` so they never block the API response. If a webhook endpoint is slow or down, the user's toggle still completes in milliseconds. Retries (3 attempts, exponential backoff) happen in the background.

**HMAC-SHA256 webhook signing** — Receivers can verify payloads haven't been tampered with by checking the `X-Canary-Signature` header. This is the same pattern used by GitHub, Stripe, and Shopify webhooks.

**Frontend served from Spring Boot** — The React build output is copied into `src/main/resources/static/app/` at Docker build time. One container, one URL, no CORS, no separate frontend server to manage or pay for.

---

## Roadmap

- **Multi-tenancy** — namespace flags per project/organization with separate API keys
- **Client SDKs** — JavaScript and Java SDKs so apps can evaluate flags without raw HTTP calls
- **Audit log** — immutable history of every flag change with timestamp and actor
- **Percentage targeting by attribute** — e.g. roll out to 10% of US users specifically, not 10% of everyone
