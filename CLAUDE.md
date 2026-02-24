# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Backend
```bash
./mvnw clean package          # Build JAR
./mvnw spring-boot:run        # Run locally (needs Postgres)
./mvnw test                   # Run all tests
./mvnw test -Dtest=TodoControllerE2ETest              # Run specific test class
./mvnw test -Dtest=TodoControllerE2ETest#methodName   # Run single test method
./mvnw -DskipTests clean package                      # Build without tests
```

### Frontend
```bash
cd frontend && npm install
npm run dev    # Dev server on :3000, proxies /api/* → localhost:8080
npm run build  # tsc + vite build → frontend/dist/
```

### Docker Compose
```bash
cp .env.example .env       # Configure first
docker-compose up -d       # Full stack (Postgres + backend :8080 + frontend :3000)
docker-compose down
```

## Architecture

### Backend (Spring Boot 3.3.4 WebFlux + R2DBC)

**Package layout** (`com.example.todowebapp`):
- `api/` — Controllers (`TodoController`, `SecurityController`)
- `service/` — Interfaces + `impl/` implementations
- `repository/` — `R2dbcRepository` interfaces (reactive)
- `domain/entity/` — DB entities extending `TimestampEntity` (audit fields)
- `domain/dto/` — Request/response DTOs
- `security/` — JWT filter chain, CORS config, custom entry points
- `config/` — `CorsProperties` (bound from `cors.*` in `application.properties`)
- `exceptions/` + `handler/` — `ErrorCode` enum, `GlobalExceptionTranslator`

**Request lifecycle:**
1. `CustomSecurityFilter` — extracts + validates JWT Bearer token, sets auth in reactor context
2. `SecurityWebFilterChain` — enforces `@PreAuthorize` privilege checks
3. Controller → Service → `R2dbcRepository` (reactive Flux/Mono throughout)

**Auth flow:**
- `POST /api/login` → `ReactiveAuthenticationManager` verifies credentials → `JwtService.generateToken()` returns JWT
- `POST /api/register` → creates user with `ROLE_BASIC_USER`, hashes password with `PasswordEncoder`
- `GET /api/me` — public but token-aware
- Roles (`ROLE_BASIC_USER`, `ROLE_ADMIN`, etc.) map to privileges (`VIEW_TODOS`, `CREATE_TODOS`, `UPDATE_TODOS`, `DELETE_TODOS`) via `roles_privileges` join table; controllers use `@PreAuthorize("hasAuthority('...')")`

**Database:**
- Flyway runs on startup: `V1__INIT_SCHEMA.sql` (tables) → `V2__INSERT_SECURITY_DATA.sql` (seed roles/privileges)
- R2DBC for reactive queries; Flyway uses a separate JDBC URL for migrations

**Tests:**
- 22 E2E tests in `TodoControllerE2ETest` use `@ActiveProfiles("test")` + H2 in-memory DB
- Test config in `src/test/resources/application-test.properties`; schema from `schema.sql`
- `WebTestClient` for reactive assertions

### Frontend (React 18 + TypeScript + Vite)

**API layer** (`src/api/`):
- `client.ts` — Axios instance with `baseURL: '/api'`; request interceptor auto-injects `Authorization: Bearer <token>` from `localStorage` key `todo_jwt`
- `auth.ts` — `login()`, `register()`, `me()`
- `todos.ts` — Todo CRUD calls

**State:** `AuthContext` / `useAuth()` hook manages token + `isAuthenticated`; token persisted to `localStorage`

**Routing:** React Router v6; `/` is protected by `ProtectedRoute` (redirects to `/login` if not authenticated)

**Dev proxy:** Vite proxies `/api/*` → `http://localhost:8080` during development. In production (Docker), nginx in the frontend container proxies `/api/` → `http://todo-backend:8080/api/` — the K8s/Compose backend service **must** be named `todo-backend` for this DNS to resolve.

## Key Configuration

**`application.properties`:**
- All DB connection params come from env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `cors.origins[0]=${FRONT_END_BASE_URL:http://localhost:3000}` — must match the browser origin
- `jwt.secret=${SECRET_KEY}`, `jwt.expiration-time=3600000`
- Actuator: all endpoints exposed (`management.endpoints.web.exposure.include=*`)
- Swagger UI: `/swagger-ui-custom.html`

**`.env` (Docker Compose):** Copy from `.env.example`. `FRONT_END_BASE_URL` must match the port the browser uses (default `http://localhost:3000`).

## Kubernetes (`k8s/`)

Deploy order: `namespace → configmap → secret → postgres → backend → frontend → ingress`

- Backend `Service` must be named `todo-backend` (nginx.conf hardcodes this DNS name)
- Liveness/readiness probes hit `/actuator/health/liveness` and `/actuator/health/readiness`
- HPA: `autoscaling/v2`, min=1 max=5, CPU target 70%
- Postgres runs as a `StatefulSet` with a PVC; headless service for stable DNS
