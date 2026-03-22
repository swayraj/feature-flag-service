# Feature Flag Service — Project Diary

---

## Day 1 — Project Bootstrap
**Commit:** `e9a3d7b`
- Created Spring Boot project
- Added Hello World REST endpoint
- Verified app starts and responds

---

## Day 2 — Flag CRUD API
**Commit:** `45405fd`
- Defined `Flag` model with id, name, enabled, rolloutPercentage
- Implemented full CRUD REST API at `/api/flags`
- In-memory storage (no database yet)

---

## Day 3 — Service Layer Refactor
**Commit:** `feaa39c`
- Introduced Service layer (separation of concerns)
- Added DTOs for request/response
- Added custom exception classes

---

## Day 4 — PostgreSQL + JPA
**Commit:** `7b45647`
- Integrated PostgreSQL database
- Added JPA/Hibernate — auto-creates tables on startup
- Replaced in-memory storage with `FlagRepository` (JpaRepository)

---

## Day 5 — Rollout Logic
**Commit:** `7a06097`
- Implemented consistent hash-based rollout evaluation
- SHA-256 hash of `flagName + userId` → bucket 0–99
- If bucket < rolloutPercentage → flag is ON for that user
- Added per-user target list (always ON regardless of %)
- Evaluation endpoints at `/api/evaluate`

---

## Day 6 — Batch Evaluation + Analytics
**Commit:** `d80847b`
- Batch evaluate one flag for many users at once
- Simulate rollout with generated user IDs
- Distribution analysis: bucket breakdown
- ASCII visual distribution chart
- Global exception handler (`@RestControllerAdvice`)

---

## Day 7 — Batch DTOs
**Commit:** `4757446`
- Extracted dedicated DTOs for batch evaluation requests/responses

---

## Day 8 — Scheduled Rollouts + Segmentation
**Commit:** `d8ca671`
- One-time scheduled rollout: set % at a specific datetime
- Auto-rollout: gradually increase % every N hours by N%
- User segmentation: filter by attributes (country, platform, etc.)

---

## Day 9 — Redis Caching
**Commit:** `2c74df7`
- Added Redis as evaluation cache with 10-minute TTL
- Cache is automatically invalidated on any flag change
- ~10x performance improvement on repeated evaluations

---

## Day 10 — WebSocket Real-Time Updates
**Commit:** `cff834c`
- Added STOMP WebSocket at `/ws`
- Clients subscribe to `/topic/flags`
- Every flag create/update/delete/toggle broadcasts a live JSON event

---

## Day 11 — Docker Containerization
**Commit:** `7fa4903`
- Multi-stage Dockerfile (build + runtime)
- `docker-compose.yml` for local dev (app + postgres + redis)
- `docker-compose-full.yml` for full stack including the app container
- Production-ready image with non-root user

---

## Day 12 — Webhook System
**Commit:** `2720119`
- Webhook registration with auto-generated HMAC-SHA256 secret
- Async delivery (`@Async`) — non-blocking
- 3 retry attempts with exponential backoff (1s → 2s)
- Smart retry: 4xx errors skip retries, 5xx/network errors retry
- Full delivery audit log per webhook
- `FlagEventService` updated to broadcast to both WebSocket and webhooks
- 8 REST endpoints at `/api/webhooks`

---

## Day 14 — CI/CD Pipeline
**Commit:** `cddcb8a`
- `build-and-test.yml`: triggers on every push, spins up real Postgres + Redis service containers, compiles and tests with Maven
- `docker-publish.yml`: triggers on push to main, builds JAR, pushes Docker image to Docker Hub with `latest` + `sha-*` tags
- Added Docker Buildx setup for GHA layer caching support
- Fixed Redis readiness check to use `nc` instead of `redis-cli` (not available on ubuntu runners)
- Both pipelines verified green on GitHub Actions

---

## Day 17 — Swagger / OpenAPI Documentation (resolved after Day 19)
**Commit:** `2780f1d`
- Original block: Spring Boot 4 migrated from Jackson 2 (`com.fasterxml.jackson`) to Jackson 3 (`tools.jackson`), but springdoc's dependency swagger-core still imported Jackson 2 classes causing a conflict. Resolved when springdoc released v3.0.2 which upgraded swagger-core to 2.2.43 with full Jackson 3 / Spring Boot 4 support.
- Added springdoc-openapi-starter-webmvc-ui v3.0.2
- Whitelisted `/swagger-ui` and `/v3/api-docs` paths in `ApiKeyFilter` so Swagger UI is publicly accessible
- Created `OpenApiConfig.java` — custom API title, version, and description
- Swagger UI available at `http://localhost:8080/swagger-ui/index.html`
- All controllers (flags, evaluation, scheduling, webhooks) automatically documented

---

## Day 19 — Performance Testing with k6
**Commit:** `7e53196`
- Installed k6 load testing tool
- `k6/smoke-test.js` — 1 VU, 10 iterations, confirmed 100% pass, avg 9ms response
- `k6/load-test.js` — 20 VUs, 50s ramp up/hold/down, 810 requests, 0 errors, p95 7.6ms
- `k6/stress-test.js` — 50 VUs, no sleep, ~627k requests, rate limiter correctly blocked 99.99% with 429s, server never crashed
- Discovered stale Docker image bug: k6 stress test showed 0 rate limiting (429s) despite correct Bucket4j code. Root cause — the running Docker image was built before Day 18, so the ApiKeyFilter with rate limiting was never included. Fixed by rebuilding the image with `./mvnw clean package` + `docker build`. After rebuild, rate limiter correctly blocked 99.99% of requests under stress.
- Custom k6 `Counter` metric used to track 429 rate-limited requests separately

---

## Day 18 — API Key Authentication + Rate Limiting
**Commit:** `eac943c`
- Added `ApiKey` model + `api_keys` table (JPA auto-created)
- `ApiKeyRepository` with `findByKeyValueAndActiveTrue` for single-query validation
- `ApiKeyFilter` (`OncePerRequestFilter`) — intercepts every request, checks `X-API-Key` header, returns 401 if missing/invalid
- Bucket4j rate limiting — 20 requests/min per API key using in-memory token bucket (`ConcurrentHashMap<String, Bucket>`)
- `data.sql` seeds a `test-key-123` dev key on startup (idempotent with `WHERE NOT EXISTS`)
- Day 17 (Swagger/OpenAPI) blocked — springdoc incompatible with Spring Boot 4 due to Jackson 2 vs Jackson 3 conflict

---

## Day 16 — Integration Tests
**Commit:** `26466cd`
- 13 integration tests in `FlagControllerIntegrationTest` — all hitting a real Postgres + Redis via TestContainers
- TestContainers spins up `postgres:15-alpine` and `redis:7-alpine` containers per test run
- Tests cover: GET all, GET by id (200 + 404), POST create (valid + duplicate + invalid), PUT update, DELETE, toggle, enabled filter, name search
- Discovered Spring Boot 4 breaking changes: Jackson 3 package moved to `tools.jackson.databind`, `AutoConfigureMockMvc` moved to `org.springframework.boot.webmvc.test.autoconfigure`
- Total test count across both days: 40 tests (27 unit + 13 integration), all green

---

## Day 15 — Unit Tests
**Commit:** `d8180ac`
- 27 unit tests across 2 test classes, all passing (0 failures)
- `FlagServiceTest` — 17 tests covering createFlag (validation, duplicates), updateFlag, deleteFlag, toggleFlag, getAllFlags, getFlagById
- `RolloutServiceTest` — 10 tests covering disabled flags, targeted users, 0%/100% rollout, consistent hashing, user segmentation
- No Spring context loaded — pure JUnit 5 + Mockito, tests run in ~3 seconds

---

## Day 13 — Kubernetes Manifests
**Commit:** `558fa7c`
- Created `k8s/` folder with 6 production-ready manifest files (10 K8s resources total)
- `configmap.yaml` — environment variables for the Spring Boot app
- `secret.yaml` — base64-encoded DB credentials, pulled into Postgres via `secretKeyRef`
- `postgres.yaml` — PVC (1Gi) + Deployment + ClusterIP Service
- `redis.yaml` — Deployment + ClusterIP Service with memory/CPU resource limits
- `app.yaml` — 2-replica Deployment with liveness/readiness probes + ClusterIP Service
- `ingress.yaml` — NGINX-based HTTP routing via `feature-flag.local`
- Added Spring Boot Actuator health probe endpoints to `application.properties`
- All 10 manifests validated with `kubectl apply --dry-run=client` against Minikube
