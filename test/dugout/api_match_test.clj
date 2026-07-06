(ns dugout.api-match-test
  (:require [clojure.test :refer [deftest is testing]]
            [dugout.api.match :as match]
            [dugout.db :as db]
            [xtdb.api :as xt]))

(deftest test-get-match-events
  (testing "Query and enrichment of match events in database"
    (let [node (db/get-node)
          match-id "match-123"
          event-id :event/evt-001
          event-doc {:xt/id event-id
                     :event/match-id match-id
                     :event/type "Shot"
                     :event/location [95.0 30.0]
                     :event/timestamp #inst "2026-07-06T12:00:00Z"}
          tx (xt/submit-tx node [[::xt/put event-doc]])]
      (xt/await-tx node tx)
      (let [events (match/get-match-events match-id)]
        (is (= 1 (count events)))
        (let [evt (first events)]
          (is (= event-id (:id evt)))
          (is (= "Shot" (get-in evt [:type :name])))
          (is (= [95.0 30.0] (:location evt)))
          ;; Check shared metrics enrichment
          (is (= :zone15 (:zone evt)))
          (is (= 0.23402207538578235 (:xg evt))))))))

(deftest test-synthetic-match-events
  (testing "Returns synthetic events for demo"
    (let [events (match/synthetic-match-events)]
      (is (vector? events))
      (is (pos? (count events)))
      (is (every? :id events))
      (is (every? :type events)))))

(deftest test-synthetic-events-contain-shots
  (testing "Synthetic events include shots with xG"
    (let [shots (->> (match/synthetic-match-events)
                     (filter #(= "Shot" (get-in % [:type :name]))))]
      (is (pos? (count shots)))
      (is (every? :xg shots)))))
