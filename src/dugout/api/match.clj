(ns dugout.api.match
  "Match data endpoints.
   Serves enriched match events with zone and xG data
   computed via shared .cljc metrics."
  (:require [dugout.db :as db]
            [dugout.shared.metrics :as metrics]
            [xtdb.api :as xt]))

(defn get-match-events
  "Returns enriched match events for a given match-id.
   Each shot event is enriched with :zone and :xg via
   the shared .cljc enrich-event function."
  [match-id]
  (let [node (db/get-node)
        db   (xt/db node)
        events (xt/q
                db
                '{:find  [?e]
                  :in    [?match-id]
                  :where [[?e :event/match-id ?match-id]]}
                match-id)]
    (->> events
         (map (fn [[id]] (xt/entity db id)))
         (map (fn [doc]
                {:id (:xt/id doc)
                 :type {:name (:event/type doc)}
                 :location (:event/location doc)
                 :timestamp (:event/timestamp doc)}))
         (map metrics/enrich-event)
         (sort-by :timestamp)
         vec)))

(defn synthetic-match-events
  "Returns synthetic match events for demo mode.
   All data is fictional — no real match data."
  []
  [{:id "evt-001" :type {:name "Pass"}
    :location [35.0 25.0] :timestamp "00:01:30"
    :zone :zone01}
   {:id "evt-002" :type {:name "Shot"}
    :location [105.0 38.0] :timestamp "00:12:45"
    :zone :zone16 :xg 0.23}
   {:id "evt-003" :type {:name "Shot"}
    :location [112.0 42.0] :timestamp "00:23:10"
    :zone :zone16 :xg 0.45}
   {:id "evt-004" :type {:name "Pass"}
    :location [60.0 40.0] :timestamp "00:35:00"
    :zone :zone09}
   {:id "evt-005" :type {:name "Shot"}
    :location [95.0 30.0] :timestamp "00:44:20"
    :zone :zone15 :xg 0.08}])
