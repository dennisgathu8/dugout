(ns dugout.shared.metrics
  "Shared metric computation running identically on JVM
   and browser via .cljc.

   THIS IS THE STRUCTURAL DIFFERENTIATOR:
   Python requires separate server and browser
   implementations of the same logic, creating permanent
   risk of divergence. These functions run on the JVM for
   batch analysis and in the browser for interactive
   tooltips — the same code, the same results.

   Proof: see test/dugout/shared_metrics_test.cljc (JVM)
   and test-cljs/dugout/shared_metrics_test.cljs (browser).
   Both test files call the same functions and assert
   the same results."
  (:require [dugout.shared.zones :as zones]))

(def ^:private goal-centre [120.0 40.0])

(defn euclidean-dist
  "Euclidean distance between two [x y] points."
  [[x1 y1] [x2 y2]]
  (#?(:clj Math/sqrt :cljs js/Math.sqrt)
   (+ (#?(:clj Math/pow :cljs js/Math.pow) (- x2 x1) 2)
      (#?(:clj Math/pow :cljs js/Math.pow) (- y2 y1) 2))))

(defn- shot-angle [[x y]]
  (let [post-top    [120.0 36.0]
        post-bottom [120.0 44.0]
        a (euclidean-dist [x y] post-top)
        b (euclidean-dist [x y] post-bottom)
        c (euclidean-dist post-top post-bottom)
        cos-c (/ (- (+ (* a a) (* b b)) (* c c))
                 (* 2 a b))
        clamped (max -1.0 (min 1.0 cos-c))]
    (#?(:clj Math/toDegrees :cljs #(* % (/ 180 js/Math.PI)))
     (#?(:clj Math/acos :cljs js/Math.acos) clamped))))

(defn estimate-xg
  "Basic distance-and-angle xG estimate.
   Returns a double in [0.0 1.0].
   Returns nil for non-shot events.

   This is the function that proves .cljc works:
   - Called server-side for batch overnight reports
   - Called client-side for interactive hover tooltips
   - Same inputs produce identical outputs on both runtimes"
  [{:keys [location under_pressure type]}]
  (when (= "Shot" (get-in type [:name]))
    (let [dist  (euclidean-dist location goal-centre)
          angle (shot-angle location)
          base  (/ angle (* dist 2.5))
          adj   (if under_pressure (* base 0.85) base)]
      (max 0.01 (min 0.95 adj)))))

(defn enrich-event
  "Adds :zone and :xg to a shot event.
   Non-shot events returned unchanged.
   Shared between batch pipeline and browser display."
  [{:keys [location type] :as event}]
  (if (and (= "Shot" (get-in type [:name])) location)
    (assoc event
           :zone (zones/compute-zone location)
           :xg   (estimate-xg event))
    event))

(defn pressing-intensity
  "Mean distance of nearest N defenders to ball carrier.
   Shared between formation-stream backend processing
   and browser live display."
  ([freeze-frame] (pressing-intensity freeze-frame 3))
  ([freeze-frame n]
   (let [actor     (->> freeze-frame
                        (filter :actor)
                        first)
         defenders (->> freeze-frame
                        (filter #(not (:teammate %)))
                        (filter #(not (:actor %)))
                        (map :location)
                        (filter some?))]
     (when (and actor (:location actor)
                (>= (count defenders) n))
       (let [dists (->> defenders
                        (map #(euclidean-dist
                               (:location actor) %))
                        sort
                        (take n))]
         (double (/ (reduce + dists) (count dists))))))))
