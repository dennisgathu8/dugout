# Security Policy — dugout

This document describes all security measures implemented in dugout.

---

## 1. JWT Authentication on Every Protected Route

Every API route except `POST /api/auth/login` and `GET /demo` requires a valid JWT token via buddy-auth. The token must be included in the `Authorization` header. Unauthenticated requests receive a 401 response.

**Implementation:** `src/dugout/auth.clj` — `require-auth` middleware wraps all protected routes.

## 2. Content Security Policy Headers

Every HTML response includes a strict CSP header:

```
Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; connect-src 'self' ws: wss:
```

This prevents XSS attacks by restricting script sources to the same origin. `unsafe-inline` is allowed for styles only (required for Reagent inline styles).

**Implementation:** `src/dugout/server.clj` — `security-headers` middleware.

## 3. WebSocket Token Authentication & Origin Validation

Every WebSocket connection request validates:
1. **Origin Header**: Checked against the configured `ALLOWED_ORIGIN` environment variable. Connections from unknown origins are rejected.
2. **JWT Authentication**: Requires a valid JWT token passed via the `token` query parameter (e.g., `/ws/tracking?token=...`). Connections with invalid or missing tokens are immediately rejected and terminated inside the `on-open` callback with a 4001 status code.

**Implementation:**
- `src/dugout/server.clj` — Extracts and forwards the query parameter token.
- `src/dugout/api/ws.clj` — `allowed-origin?` function and token verification inside `ws-listener`.

## 4. Source Maps Disabled in Production

The shadow-cljs release build has `:source-maps false`. Source maps expose unminified ClojureScript source code to anyone with browser DevTools open.

**Implementation:** `shadow-cljs.edn` — `:release` build configuration.

## 5. XTDB Data Directory from Environment Only

The XTDB data directory is read from `XTDB_DATA_PATH` environment variable only. It is never hardcoded. If the variable is not set and demo mode is not enabled, the application throws a configuration error.

**Implementation:** `src/dugout/db.clj` — `start-node` function.

## 6. JWT Secret from Environment Only

The JWT signing secret is read from `JWT_SECRET` environment variable only. It is never hardcoded in source code. The secret must be at least 32 characters. For production deployments, generate with `openssl rand -hex 32`.

**Implementation:** `src/dugout/auth.clj` — `jwt-secret` function.

## 7. No Player Biometric Data in Test Fixtures

All test data uses synthetic/fictional player names and IDs. No real player biometric data, medical records, or performance data appears in any test fixture, seed data, or demo mode output.

**Implementation:** `src/dugout/api/player.clj` — `synthetic-squad` returns fictional data only.

## 8. nREPL Bound to localhost Only

The REPL server is bound to `127.0.0.1` only, preventing remote connections. This is configured in `project.clj` under `:repl-options`.

**Implementation:** `project.clj` — `:repl-options {:host "127.0.0.1"}`.

## 9. HTTPS Enforced on fly.io

The `fly.toml` deployment configuration sets `force_https = true` under `[http_service]`. All HTTP requests are automatically redirected to HTTPS.

**Implementation:** `fly.toml` — `force_https = true`.

## 10. No Secrets in ClojureScript Bundles

The ClojureScript frontend never receives database credentials, XTDB paths, API keys, or JWT signing secrets. The frontend receives only a JWT token after successful login, which it uses for subsequent API calls. The token contains claims (user, role, expiry) but not the signing secret.

**Implementation:** All secrets are server-side only in `src/dugout/auth.clj` and `src/dugout/db.clj`. The ClojureScript source paths (`src-cljs/`) never import these namespaces.

---

## Kenya Data Protection Act 2019

Player performance and fitness data constitutes personal data under the Kenya Data Protection Act 2019. Clubs using this system with real player data must:

1. Obtain player consent for data processing
2. Implement appropriate data retention policies
3. Ensure data does not leave club-controlled infrastructure (see Data Sovereignty statement in README)
4. Maintain audit logs of data access

This system's architecture supports these requirements: all data remains on club-controlled infrastructure, and XTDB's immutable log provides a complete audit trail.

---

## Reporting Vulnerabilities

Report security vulnerabilities to the repository maintainer via GitHub private vulnerability reporting. Do not open public issues for security concerns.
