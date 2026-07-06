(ns dugout.api-player-test
  (:require [clojure.test :refer [deftest is testing]]
            [dugout.api.player :as player]
            [dugout.db :as db]
            [xtdb.api :as xt]))

(deftest test-available-players-bi-temporal
  (testing "Bi-temporal queries with available-players"
    (let [node (db/get-node)
          player-id :player/test-p1
          doc {:xt/id player-id
               :player/name "Test Player"
               :player/fatigue-idx 0.5
               :player/available? true}
          vt1 #inst "2026-05-01T00:00:00Z"
          tx (xt/submit-tx node [[::xt/put doc vt1]])]
      (xt/await-tx node tx)
      ;; Querying before vt1 should return empty
      (is (empty? (player/available-players #inst "2026-04-30T00:00:00Z")))
      ;; Querying at vt1 should return the player
      (let [results (player/available-players vt1)]
        (is (= 1 (count results)))
        (is (= [player-id "Test Player" 0.5 true] (first results)))))))

(deftest test-synthetic-squad
  (testing "Returns synthetic squad data"
    (let [squad (player/synthetic-squad)]
      (is (vector? squad))
      (is (= 5 (count squad)))
      (is (every? :name squad))
      (is (every? :fatigue squad))
      (is (every? #(contains? % :available) squad)))))

(deftest test-synthetic-squad-no-real-data
  (testing "No real player data in synthetic squad"
    (let [squad (player/synthetic-squad)
          names (map :name squad)]
      ;; All names are fictional
      (is (every? string? names))
      (is (not-any? #(= "Real Player" %) names)))))

(deftest test-fatigue-ranges
  (testing "Fatigue values in valid range"
    (let [squad (player/synthetic-squad)]
      (is (every? #(<= 0.0 (:fatigue %) 1.0) squad)))))
