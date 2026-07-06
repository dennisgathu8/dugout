# ADR-002: Reagent + re-frame for Frontend

## Status
Accepted

## Context
dugout needs a ClojureScript UI framework for five browser views: Squad Availability, Pressing Analysis Map, Player Timeline, International Break Impact, and Live Tracking Monitor.

The framework must support:
- Reactive data binding (squad data updates, live tracking)
- Predictable state management (five views sharing state)
- WebSocket integration (live tracking monitor)
- SVG rendering (pressing analysis pitch map)

## Options Considered

### 1. Reagent only
Minimal React wrapper for ClojureScript. Simple, lightweight, uses ratoms for state. No structured state management pattern.

### 2. Reagent + re-frame
Reagent for rendering + re-frame for structured state management via events, subscriptions, and effects. Adds ceremony but provides predictability.

### 3. Fulcro
Full-featured framework with client-server data synchronization, form management, and routing. Powerful but heavyweight for this application.

### 4. Helix
Modern React wrapper using hooks. Closer to idiomatic React but less mature in the ClojureScript ecosystem.

## Decision
**Option 2: Reagent + re-frame.**

re-frame's event/subscription model provides predictable state management across five views that share squad data, rules, and tracking state. The ceremony (registering events, subscriptions, and effects) is justified by the debuggability it provides.

## Consequences

### Positive
- **Predictable state:** All state changes go through registered events — no hidden mutations
- **Derived data:** Subscriptions like `:squad-by-availability` and `:fatigue-high-risk` compute derived data cleanly
- **Effect isolation:** HTTP calls and WebSocket connections are registered effects, testable independently
- **Community support:** re-frame is the most widely used ClojureScript state management library

### Negative
- More boilerplate than pure Reagent (event registration, subscription registration)
- Learning curve for developers unfamiliar with the event/subscription pattern
- Overhead for simple state changes that could be a single ratom swap

### Risks
- re-frame's global app-db could become unwieldy if the application grows significantly
- WebSocket integration requires custom effect handlers
