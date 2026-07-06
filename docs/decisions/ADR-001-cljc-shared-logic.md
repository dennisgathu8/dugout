# ADR-001: Shared Logic via .cljc

## Status
Accepted

## Context
dugout needs xG estimation, zone computation, and pressing metric definitions that run in two places:
1. **Server-side** (JVM) — for batch overnight reports, API responses, and XTDB-backed analysis
2. **Client-side** (Browser) — for interactive tooltips, hover effects, and real-time display

The risk of implementing these computations separately in Clojure (server) and ClojureScript (browser) is permanent divergence: a fix applied to one runtime but not the other produces silent inconsistencies.

## Options Considered

### 1. Duplicate code
Maintain separate `.clj` and `.cljs` implementations of the same functions. Simple but creates divergence risk. Every bug fix must be applied twice.

### 2. REST API call per hover
Compute everything server-side and make an API call for every tooltip/hover interaction. Eliminates divergence but adds latency to every user interaction and requires network connectivity.

### 3. .cljc shared namespace
Use Clojure's reader conditionals (`#?(:clj ... :cljs ...)`) to write functions that compile to both JVM bytecode and JavaScript. One source file, two runtimes, same results.

## Decision
**Option 3: .cljc shared namespaces.**

Three shared files:
- `metrics.cljc` — xG estimation, pressing intensity, event enrichment
- `zones.cljc` — 18-zone pitch computation
- `rules.cljc` — pressing rule data structures

Platform-specific operations (e.g., `Math/sqrt` vs `js/Math.sqrt`) use reader conditionals.

## Consequences

### Positive
- **One truth:** Same function, same file, same results on both runtimes
- **Testable proof:** JVM tests (`lein test`) and ClojureScript tests (`node target/test/test.js`) run the same assertions
- **No divergence risk:** Bug fixes in shared logic automatically apply to both runtimes
- **Portfolio signal:** Demonstrates a structural advantage of Clojure over Python

### Negative
- Cannot use platform-specific APIs without reader conditionals
- `.cljc` files must avoid JVM-only or JS-only dependencies
- Developers must understand reader conditional syntax

### Risks
- Complex platform-specific logic may be harder to express in `.cljc`
- Testing must cover both runtimes explicitly

## Proof
See `test/dugout/shared_metrics_test.cljc` (JVM) and `test-cljs/dugout/shared_metrics_test.cljs` (browser). Both call the same functions with the same inputs and assert the same results.
