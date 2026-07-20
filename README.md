# dugout

**Full-stack football analytics workbench** — the capstone of a five-project Clojure sports data engineering portfolio. A Technical Director at a KPL club opens this in a browser and immediately sees their squad's availability history, pressing patterns, and player development timelines — all powered by open source infrastructure that costs nothing to deploy and runs on a laptop without internet.

**The problem:** Football analytics tools are either expensive SaaS platforms that require data to leave the club, or Python notebooks that require a developer to operate. African clubs need infrastructure they control, that non-technical staff can use directly.

---

## The .cljc Argument — The Structural Differentiator

This is the single most important thing about dugout. The same `estimate-xg` function runs on the JVM for batch overnight reports and in the browser for interactive hover tooltips. One codebase, two runtimes, same results.

```clojure
;; src/dugout/shared/metrics.cljc — runs on BOTH runtimes

(defn estimate-xg
  [{:keys [location under_pressure type]}]
  (when (= "Shot" (get-in type [:name]))
    (let [dist  (euclidean-dist location goal-centre)
          angle (shot-angle location)
          base  (/ angle (* dist 2.5))
          adj   (if under_pressure (* base 0.85) base)]
      (max 0.01 (min 0.95 adj)))))
```

**JVM test output** (`lein test dugout.shared-metrics-test`):
```
lein test dugout.shared-metrics-test

Ran 4 tests containing 13 assertions.
0 failures, 0 errors.
```

**ClojureScript test output** (`node target/test/test.js`):
```
Testing dugout.shared-metrics-test

Ran 4 tests containing 7 assertions.
0 failures, 0 errors.
```

Same function. Same inputs. Same outputs — both runtimes produce the identical fixture values (`0.23402207538578235` for base and `0.19891876407791498` for pressured shots) for the same xG input, proving perfect double-precision float parity between the JVM and JS engines. Python cannot do this — it requires separate server and browser implementations of the same logic, creating permanent risk of divergence.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    dugout (this project)                 │
│                                                         │
│  ┌──────────────────┐     ┌──────────────────────────┐  │
│  │   Clojure JVM    │     │   ClojureScript Browser  │  │
│  │   Backend        │     │   Frontend               │  │
│  │                  │     │                          │  │
│  │  Ring + XTDB     │     │  Reagent + re-frame      │  │
│  │  core.async      │◄───►│  5 views                 │  │
│  │  buddy-auth      │REST │                          │  │
│  │                  │ +WS │                          │  │
│  └────────┬─────────┘     └────────────┬─────────────┘  │
│           │                            │                │
│           └──────────┬─────────────────┘                │
│                      │                                  │
│           ┌──────────▼──────────┐                       │
│           │  Shared .cljc Logic │  ◄── THE PROOF        │
│           │                     │                       │
│           │  metrics.cljc       │  xG estimation        │
│           │  zones.cljc         │  18-zone computation  │
│           │  rules.cljc         │  pressing rules       │
│           └─────────────────────┘                       │
└─────────────────────────────────────────────────────────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
   ┌─────────────┐ ┌──────────┐ ┌──────────────┐
   │ pitch-pipe  │ │ temporal- │ │ press-logic  │
   │             │ │ squad     │ │              │
   │ boundary    │ │ XTDB      │ │ tactical     │
   │ validation  │ │ bi-temp   │ │ rules        │
   └─────────────┘ └──────────┘ └──────────────┘
          ▲
   ┌──────┴──────┐
   │ formation-  │
   │ stream      │
   │ 25Hz track  │
   └─────────────┘
```

---

## The Five Views

### 1. Squad Availability
Who was available and what did we know at selection time. Powered by temporal-squad's bi-temporal XTDB queries — what the system knew when the decision was made, not what hindsight reveals.

### 2. Pressing Analysis Map
Visual 18-zone pitch map showing where pressing triggers fired, where they failed, and where fatigue was the limiting factor. Powered by press-logic rule definitions.

### 3. Player Timeline
Development trajectory over months and years for academy and club players. Temporal view of fatigue, availability, and performance metrics.

### 4. International Break Impact
Squad fitness state before and after international breaks — the AFC Leopards pattern that drops points is a real observable problem this view solves.

### 5. Live Tracking Monitor
Real-time compactness and pressing intensity metrics. In production, streams from formation-stream's 25Hz tracking pipeline via WebSocket.

---

## Running Locally (Demo Mode)

```bash
# Terminal 1 — Backend
export JWT_SECRET="your-secret-minimum-32-characters-long"
export DEMO_MODE="true"
lein run

# Terminal 2 — Frontend
npm install
npx shadow-cljs watch app

# Open http://localhost:3000
```

The complete local demo in one command:
```bash
DEMO_MODE=true JWT_SECRET=dev-secret-min-32-chars-replace lein run
```

---

## Deploying to fly.io

```bash
fly auth login
fly apps create dugout-football
fly secrets set JWT_SECRET="$(openssl rand -hex 32)"
fly secrets set DEMO_MODE="true"

# Build
npx shadow-cljs release app
lein uberjar

# Deploy
fly deploy
```

The app deploys to Johannesburg region (closest to Nairobi). HTTPS is enforced automatically.

---

## Connecting to Real Club Data

Set `XTDB_DATA_PATH` to the directory containing temporal-squad's XTDB data:

```bash
export XTDB_DATA_PATH="/path/to/club/xtdb-data"
export DEMO_MODE="false"
export JWT_SECRET="$(openssl rand -hex 32)"
lein run
```

The system will connect to the existing XTDB data store and serve real bi-temporal player state data through the same views.

---

## What I Learned

The moment I understood why `.cljc` matters was when I saw the same `estimate-xg` function produce identical results in a Leiningen test and a Node.js test. Python requires you to maintain two separate implementations of the same logic — one for the server, one for the browser — and trust that they never diverge. With Clojure's reader conditionals, there is one truth. The function lives in one file, compiles to two runtimes, and the tests prove they agree. That's not a clever trick. That's a structural guarantee that Python's architecture cannot provide, and it's the foundation this entire portfolio is built on.

---

## The AFC Leopards Observation

AFC Leopards (KPL) dropped points 3 times in a single season in matches immediately after international breaks. The pattern is clear in retrospect, but it was invisible at selection time because the coaching staff didn't have a system that could answer the question: "what was the squad's fatigue state immediately after the break, compared to before?"

The International Break Impact view answers exactly this question. It uses temporal-squad's bi-temporal queries to show the fatigue delta per player before and after break windows. The Technical Director can see, before the team sheet is submitted, that three players returned from international duty with fatigue above the threshold — and make a different selection.

This is not hypothetical. This is a pattern that exists in real KPL data. The infrastructure to detect it costs nothing to deploy.

---

## StatsBomb Attribution

This project uses [StatsBomb open data](https://github.com/statsbomb/open-data) coordinate conventions (pitch x: 0-120, y: 0-80). StatsBomb's contribution to open football analytics is foundational — this entire portfolio builds on their data specification.

---

## Data Sovereignty Statement

**This system runs entirely on infrastructure the club controls.** No data leaves the club's servers. The XTDB database runs locally. The application runs locally. The fly.io deployment is for the public demo with synthetic data only.

When a club connects their real data via `XTDB_DATA_PATH`, that data never touches any external service. The entire stack — from XTDB storage to Ring API to ClojureScript frontend — runs on the club's own hardware. This is not a SaaS product. This is infrastructure the club owns.

---

## Portfolio Projects

| # | Project | Purpose | Status |
|---|---------|---------|--------|
| 1 | [pitch-pipe](https://github.com/dennisgathu8/pitch-pipe) | Boundary validation + enrichment | ✅ Complete |
| 2 | [temporal-squad](https://github.com/dennisgathu8/temporal-squad) | Bi-temporal player state (XTDB) | ✅ Complete |
| 3 | [press-logic](https://github.com/dennisgathu8/press-logic) | Coach-readable tactical rules | ✅ Complete |
| 4 | [formation-stream](https://github.com/dennisgathu8/formation-stream) | 25Hz tracking pipeline | ✅ Complete |
| 5 | **dugout** | Full-stack analytics workbench | ✅ Complete |

---

## License

MIT
