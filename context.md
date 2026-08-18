# URL Shortener — Repository Context

## Purpose

This repository implements a full-stack URL shortener. Users can create a short link, follow it through an HTTP redirect, resolve a short ID, view click counts, and see the 10 most recently created links.

## Repository layout

- `frontend/` — React 19 + TypeScript single-page UI built with Vite 8 and Tailwind CSS 4.
- `backend/` — Java 17 / Spring Boot 3.5 REST service built with Maven.
- `backend/docker-compose.yml` — local MongoDB 6 and Redis 7 infrastructure. The application itself is not containerized.

## Architecture and request flow

The browser calls the backend through an Axios client configured by `VITE_API_BASE_URL`. `UrlController` exposes the REST API and delegates persistence/caching behavior to `UrlService`.

For a new URL, the service normalizes a missing scheme to HTTPS, checks MongoDB for an existing mapping, increments a Redis counter, Base62-encodes that numeric value, stores the mapping in MongoDB, and caches the short-ID lookup in Redis for seven days. Both `shortId` and `originalUrl` have unique MongoDB indexes, so repeated URLs reuse an existing mapping and save-time races receive a database-backed retry.

Redirect resolution follows a cache-aside path: Redis is checked first, with MongoDB as the fallback. Successful redirects increment a Redis click key instead of writing MongoDB synchronously. `ClickSyncService` periodically (60 seconds by current configuration) folds these counters into MongoDB and deletes each Redis counter only after a successful save. The stats endpoint combines persisted and pending Redis counts.

## Backend

Key technologies: Spring Web, Spring Data MongoDB, Spring Data Redis, Bean Validation, Lombok, Maven, and JUnit 5/Spring Boot Test. MapStruct is declared in `pom.xml` but is not used in the current source.

Important files:

- `UrlController.java` — `/shorten`, `/{shortId}`, `/stats/{shortId}`, `/resolve/{shortId}`, and `/recent` endpoints; currently permits CORS from any origin.
- `UrlService.java` — URL normalization/deduplication, Base62 ID generation, Redis cache-aside resolution, click aggregation, and recent-link mapping.
- `ClickSyncService.java` — scheduled Redis-to-MongoDB click persistence.
- `Url.java` / `UrlRepo.java` — MongoDB document, unique indexes, lookup methods, and the top-10 recent query.
- `Base62.java` — converts the Redis-backed numeric sequence into compact alphanumeric IDs.
- `UrlShortnerApplication.java` — enables MongoDB auditing and scheduled jobs.
- `application.yaml` — service port (`8088`), data-store settings, logging, and click-sync interval.

API behavior:

- `POST /shorten` with `{ "url": "..." }` returns a wrapped `ShortenResponse` containing `shortId`.
- `GET /{shortId}` returns HTTP 302 with a `Location` header when found and records a click.
- `GET /stats/{shortId}` returns the combined persisted and pending click count.
- `GET /resolve/{shortId}` returns the original URL without recording a click.
- `GET /recent` returns the 10 newest mappings by `createdAt`.

## Frontend

The single-page UI is split into three components:

- `ShortenBlock` submits a URL, builds the generated short link, and fetches its click count.
- `ResolveBlock` builds a redirect link from an entered short ID.
- `HistoryBlock` loads and renders the recent-link list on mount.

Styling uses Tailwind utility classes with a responsive one/two/three-column layout, gradient/glass effects, and small custom animations. Axios centralizes API access in `src/services/api.ts`; shared TypeScript interfaces live in `src/types/url.ts`. `react-hot-toast` and `lucide-react` are installed but are not currently used.

## Running locally

Prerequisites: Java 17, Docker/Compose (or separately running MongoDB and Redis), and a Node.js version compatible with Vite 8.

1. Start MongoDB and Redis from `backend/` with Docker Compose.
2. Configure the backend MongoDB connection and Redis host/port. Secrets should be supplied through environment variables rather than committed files.
3. Run the backend from `backend/` with `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
4. Set `VITE_API_BASE_URL` to the backend base URL (normally `http://localhost:8088`), then run `npm install` and `npm run dev` from `frontend/`.

Useful checks: `./mvnw test`, `npm run lint`, and `npm run build`.

## Testing, infrastructure, and maturity

- Backend testing currently consists only of a Spring context-load test; service, controller, Redis/Mongo integration, redirect, race-handling, and click-sync behavior are untested.
- No frontend test framework or test files are present.
- Docker Compose provisions development data stores and persists MongoDB data, but there are no application Dockerfiles, CI workflows, deployment manifests, health checks, or observability integrations.
- The application is an early working implementation (one repository commit) with some debug logging and unused imports/dependencies still present.
- `@Valid` is not applied to the shorten controller argument, so the `@NotBlank` DTO constraint is not currently enforced.
- The checked-in MongoDB URI includes credentials. Rotate any live credential, remove it from tracked configuration/history as appropriate, and load it from environment/secret management. Restrict wildcard CORS before production use.
- Redis `KEYS clicks:*` is simple but can block on a large keyspace; a scan/stream/queue-based approach would be safer at scale. The read-then-save click flush can also lose increments under concurrent sync/update timing, so production hardening should use atomic transfer/idempotency.

## Resume bullet options

- Built a full-stack URL-shortening application with React 19, TypeScript, Spring Boot 3.5, MongoDB, and Redis, supporting link creation, HTTP redirects, resolution, click statistics, and recent-link history.
- Designed compact short IDs by combining a Redis-backed monotonic counter with Base62 encoding, while retaining a timestamp fallback for counter failures.
- Implemented cache-aside URL resolution with Redis and MongoDB, including cache repopulation on misses and seven-day caching for newly created or deduplicated mappings.
- Reduced synchronous redirect-path writes by accumulating click events in Redis and scheduling batched persistence to MongoDB, while reporting totals across persisted and pending counts.
- Enforced unique MongoDB indexes on both short IDs and original URLs and added retry logic to recover an existing mapping when concurrent inserts race.
- Created a typed React/Vite interface with dedicated shortening, resolution, and history components, a centralized Axios API layer, and a responsive Tailwind CSS layout.
- Exposed REST endpoints for shortening, 302 redirects, non-mutating URL resolution, click analytics, and a top-10 recent-links feed using Spring Data repository queries and Java records.
- Added local MongoDB and Redis provisioning through Docker Compose and configurable backend/frontend connection settings for reproducible development.

Quantification opportunities: replace or extend these bullets only after measuring real values, such as cache-hit rate, redirect latency before/after Redis, sustained requests per second, click-sync batch volume, test coverage, or deployment/user counts. Do not claim those metrics until they have been measured.
