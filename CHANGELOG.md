# Changelog

All notable changes to dugout will be documented in this file.

## [Unreleased]

### Added
- Full-stack football analytics workbench
- Squad Availability view (temporal-squad integration)
- Pressing Analysis Map (press-logic + 18-zone visual)
- Player Timeline view (temporal-squad bi-temporal history)
- International Break Impact view (AFC Leopards insight)
- Live Tracking Monitor view (formation-stream WebSocket)
- .cljc shared metrics — same xG function on JVM and browser
- .cljc shared zone computation — 18-zone pitch mapping
- .cljc shared pressing rule data structures
- JWT authentication via buddy-auth on all protected routes
- Content Security Policy headers on all responses
- WebSocket Origin validation
- Demo mode — no real club data required
- Synthetic squad data with fictional players
- fly.io deployment configuration (Johannesburg region)
- Dockerfile for production deployment
- GitHub Actions CI with JVM + ClojureScript tests
- Three Architecture Decision Records (ADRs)
- SECURITY.md documenting all security measures
- Dev namespace with REPL convenience functions
