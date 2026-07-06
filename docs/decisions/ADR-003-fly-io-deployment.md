# ADR-003: fly.io for Deployment

## Status
Accepted

## Context
dugout needs a public demo accessible to KPL clubs and potential partners. The demo must:
- Run with synthetic data only (no real club data)
- Be accessible via HTTPS
- Be affordable (ideally free tier)
- Have low latency from East Africa

## Options Considered

### 1. fly.io
Global edge deployment with Johannesburg region (closest to Nairobi). Free tier available. Automatic HTTPS. Docker-based deployment.

### 2. Railway
Simple deployment platform with auto-scaling. No African regions available. Free tier limited.

### 3. Heroku
Established platform but no African regions. Free tier discontinued. Cold starts on basic tier.

### 4. Self-hosted VPS
Full control but requires infrastructure management. Hetzner has no African regions. DigitalOcean has no African regions.

## Decision
**Option 1: fly.io (Johannesburg region).**

Johannesburg is the closest available region to Nairobi (~3,500 km vs ~8,000 km for European regions). fly.io provides:
- Automatic HTTPS with `force_https = true`
- Docker-based deployment (simple uberjar packaging)
- Auto-stop/auto-start machines (cost optimization)
- Free tier sufficient for portfolio demos

## Consequences

### Positive
- **Low latency:** Johannesburg region provides ~50ms RTT to Nairobi vs ~150ms for European regions
- **Free tier:** Sufficient for portfolio demo with auto-stop machines
- **Automatic HTTPS:** No certificate management needed
- **Simple deployment:** `fly deploy` with Dockerfile

### Negative
- Vendor dependency on fly.io platform
- Johannesburg is still 3,500 km from Nairobi (no East African regions available)
- Auto-stop machines introduce cold start latency on first request

### Risks
- fly.io pricing changes could affect free tier availability
- Platform outages affect demo availability
- The production deployment for real clubs should be self-hosted (see Data Sovereignty in README)

## Configuration
```toml
app = "dugout-football"
primary_region = "jnb"
force_https = true
```

Secrets managed via `fly secrets set` — JWT_SECRET and DEMO_MODE are never in source control.
