(ns dugout.api.player
  "Squad availability and player state API.
   Queries temporal-squad's XTDB store for bi-temporal
   player state data."
  (:require [dugout.db :as db]
            [xtdb.api :as xt]))

(defn available-players
  "Returns all available players at a given valid-time
   and transaction-time. The bi-temporal query that
   answers: 'who did we think was available on selection
   day, based on what we knew at that moment?'

   valid-time — match date (java.util.Date)
   tx-time    — when data was loaded (java.util.Date)
                defaults to nil (latest tx-time) if not provided"
  ([valid-time] (available-players valid-time nil))
  ([valid-time tx-time]
   (let [db (if tx-time
              (xt/db (db/get-node) valid-time tx-time)
              (xt/db (db/get-node) valid-time))]
     (xt/q db
      '{:find  [?id ?name ?fatigue ?available]
        :where [[?id :player/name      ?name]
                [?id :player/fatigue-idx ?fatigue]
                [?id :player/available? ?available]]}))))

(defn player-history
  "Full valid-time history of a single player."
  [player-id]
  (let [node (db/get-node)
        history (xt/entity-history
                 (xt/db node)
                 (keyword "player" player-id)
                 :asc
                 {:with-docs? true})]
    (mapv (fn [h]
            {:xtdb.api/valid-time (str (.toInstant (:xtdb.api/valid-time h)))
             :xtdb.api/tx-time    (str (.toInstant (:xtdb.api/tx-time h)))
             :xtdb.api/doc        (:xtdb.api/doc h)})
          history)))

(defn international-break-impact
  "Compares squad fatigue state immediately before and
   after an international break window.
   Returns a map of player-id to fatigue delta.

   This directly addresses the AFC Leopards pattern:
   3 dropped-points matches immediately after
   international breaks in a single season."
  [pre-break-date post-break-date]
  (let [pre  (available-players pre-break-date)
        post (available-players post-break-date)
        pre-map (into {} (map (fn [[id _ f _]] [id f]) pre))]
    (->> post
         (map (fn [[id name post-fatigue _]]
                {:player-id id
                 :name name
                 :fatigue-before (get pre-map id 0.0)
                 :fatigue-after  post-fatigue
                 :fatigue-delta  (- post-fatigue
                                    (get pre-map id 0.0))}))
         (sort-by :fatigue-delta >)
         vec)))

(defn synthetic-squad
  "Returns synthetic squad data for demo mode.
   No real player data — all names and IDs are fictional."
  []
  [{:player-id :player/p101 :name "Amara Diallo"
    :fatigue 0.35 :available true}
   {:player-id :player/p202 :name "Keza Ndayisaba"
    :fatigue 0.62 :available true}
   {:player-id :player/p303 :name "Tendai Moyo"
    :fatigue 0.78 :available true}
   {:player-id :player/p404 :name "Kofi Mensah"
    :fatigue 0.45 :available true}
   {:player-id :player/p505 :name "Emeka Okafor"
    :fatigue 0.91 :available false}])
