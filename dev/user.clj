(ns user
  "Dev namespace for REPL-driven development.
   Provides convenience functions for starting/stopping
   the XTDB node and inspecting state."
  (:require [dugout.db :as db]
            [dugout.auth :as auth]
            [dugout.api.player :as player]
            [dugout.api.rules :as rules]
            [dugout.shared.metrics :as metrics]
            [dugout.shared.zones :as zones]))

(defn start []
  (db/get-node)
  (println "dugout XTDB node started"))

(defn stop []
  (db/stop-node!)
  (println "dugout XTDB node stopped"))

(defn demo-data []
  {:squad (player/synthetic-squad)
   :rules (rules/get-rules)})

(comment
  ;; Start/stop XTDB
  (start)
  (stop)

  ;; Get demo data
  (demo-data)

  ;; Generate a demo token
  (auth/demo-token)

  ;; Test shared .cljc functions on JVM
  (zones/compute-zone [60.0 40.0])   ;; => :zone10
  (zones/compute-zone [110.0 40.0])  ;; => :zone16

  (metrics/estimate-xg
   {:type {:name "Shot"}
    :location [110.0 40.0]})

  (metrics/euclidean-dist [0.0 0.0] [3.0 4.0]) ;; => 5.0
  )
