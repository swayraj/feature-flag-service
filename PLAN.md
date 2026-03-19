# Feature Flag Service — 30-Day Build Plan

## Goal
Build a production-grade Feature Flag Service from scratch in Java/Spring Boot, covering
backend API, infrastructure, DevOps, and a frontend admin UI over 30 days.

---

## Week 1 — Core Backend (Days 1–9)

| Day | Goal | Status |
|-----|------|--------|
| Day 1 | Spring Boot project setup, Hello World endpoint | ✅ Done |
| Day 2 | Flag model + full CRUD REST API | ✅ Done |
| Day 3 | Service layer refactor, DTOs, custom exceptions | ✅ Done |
| Day 4 | PostgreSQL integration with JPA/Hibernate | ✅ Done |
| Day 5 | Rollout logic — consistent hash-based % targeting + user lists | ✅ Done |
| Day 6 | Batch evaluation, simulation, distribution analysis, global exception handler | ✅ Done |
| Day 7 | Batch Evaluation DTOs | ✅ Done |
| Day 8 | Scheduled rollouts, auto-rollout (gradual %), user segmentation | ✅ Done |
| Day 9 | Redis caching — 10-min TTL, auto-invalidation on change | ✅ Done |

---

## Week 2 — Real-Time & Integrations (Days 10–14)

| Day | Goal | Status |
|-----|------|--------|
| Day 10 | WebSocket (STOMP) — real-time flag change broadcasts | ✅ Done |
| Day 11 | Docker — multi-stage Dockerfile + docker-compose (app + postgres + redis) | ✅ Done |
| Day 12 | Webhook system — async delivery, 3-retry + backoff, HMAC-SHA256 signing | ✅ Done |
| Day 13 | Kubernetes — full stack manifests (ConfigMap, Secret, PVC, Deployments, Services, Ingress, health probes) | ✅ Done |
| Day 14 | CI/CD — GitHub Actions pipeline (build, test, Docker push) | ✅ Done |

---

## Week 3 — DevOps & Testing (Days 15–21)

| Day | Goal | Status |
|-----|------|--------|
| Day 15 | Unit tests — service layer (JUnit 5 + Mockito) | ✅ Done |
| Day 16 | Integration tests — full API tests with TestContainers (real Postgres + Redis) | ⬜ |
| Day 17 | API documentation — Swagger/OpenAPI (springdoc) | ⬜ |
| Day 18 | Rate limiting + security hardening (API keys or JWT) | ⬜ |
| Day 19 | Performance testing — load test with k6 or JMeter | ⬜ |
| Day 20 | Buffer / catch-up day | ⬜ |
| Day 21 | Buffer / catch-up day | ⬜ |

---

## Week 4 — Frontend & Polish (Days 22–30)

| Day | Goal | Status |
|-----|------|--------|
| Day 22 | Admin Web UI — HTML/JS or React scaffold, flag list view | ⬜ |
| Day 23 | Admin UI — create/edit/delete flags, toggle switch | ⬜ |
| Day 24 | Admin UI — rollout % slider, scheduled rollout form | ⬜ |
| Day 25 | Admin UI — webhook management panel | ⬜ |
| Day 26 | Admin UI — real-time live feed via WebSocket | ⬜ |
| Day 27 | Analytics dashboard — flag evaluation stats, delivery success rates | ⬜ |
| Day 28 | Final performance optimizations + caching review | ⬜ |
| Day 29 | Final documentation (README, architecture diagram) | ⬜ |
| Day 30 | Demo video + project wrap-up | ⬜ |

---

## Architecture (Current)

```
Client / Admin
     │
     ▼
REST API (port 8080)
     │
     ├── FlagService          → CRUD, validation, business rules
     ├── RolloutService       → Evaluates flags per user (hashing)
     ├── SchedulingService    → Scheduled + auto-gradual rollouts
     ├── FlagEventService     → Broadcasts to WebSocket + Webhooks
     └── WebhookService       → HTTP callbacks (async, HMAC-signed)
     │
     ├── PostgreSQL           → Source of truth
     ├── Redis                → Evaluation cache (10-min TTL)
     └── WebSocket (/ws)      → Real-time push to clients
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Database | PostgreSQL (JPA/Hibernate) |
| Cache | Redis (10-min TTL) |
| Real-time | WebSocket (STOMP) |
| HTTP Callbacks | RestTemplate + HMAC-SHA256 |
| Container | Docker + Docker Compose |
| Orchestration | Kubernetes (Minikube) — manifests validated |
| CI/CD | GitHub Actions — upcoming |
| Frontend | HTML/JS or React — upcoming |
