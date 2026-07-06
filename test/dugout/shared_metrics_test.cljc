(ns dugout.shared-metrics-test
  (:require [clojure.test :refer [deftest is testing]]
            [dugout.shared.metrics :as metrics]
            [dugout.shared.zones   :as zones]))

(deftest test-compute-zone
  (testing "Known zone coordinates"
    (is (= :zone10 (zones/compute-zone [60.0 40.0])))
    (is (= :zone01 (zones/compute-zone [5.0 5.0])))
    (is (= :zone16 (zones/compute-zone [110.0 40.0])))))

(def test-xg-fixture-value 0.23402207538578235)
(def test-xg-pressured-fixture-value 0.19891876407791498)

(deftest test-estimate-xg
  (testing "Returns nil for non-shot event"
    (is (nil? (metrics/estimate-xg
               {:type {:name "Pass"}
                :location [60.0 40.0]}))))
  (testing "Returns double in [0.0 1.0] for shot"
    (let [xg (metrics/estimate-xg
              {:type {:name "Shot"}
               :location [95.0 30.0]})]
      (is (double? xg))
      (is (>= xg 0.0))
      (is (<= xg 1.0))))
  (testing "Under pressure reduces xG"
    (let [base (metrics/estimate-xg
                {:type {:name "Shot"}
                 :location [95.0 30.0]})
          pressured (metrics/estimate-xg
                     {:type {:name "Shot"}
                      :location [95.0 30.0]
                      :under_pressure true})]
      (is (< pressured base))
      (is (= test-xg-fixture-value base))
      (is (= test-xg-pressured-fixture-value pressured)))))

(deftest test-euclidean-dist
  (testing "Pythagorean triple"
    (is (= 5.0 (metrics/euclidean-dist [0.0 0.0] [3.0 4.0])))))

(deftest test-pressing-intensity
  (testing "Returns nil when no actor"
    (is (nil? (metrics/pressing-intensity []))))
  (testing "Returns positive double for valid input"
    (let [ff [{:actor true :teammate true :location [65.0 40.0]}
              {:actor false :teammate false :location [55.0 35.0]}
              {:actor false :teammate false :location [58.0 42.0]}
              {:actor false :teammate false :location [52.0 38.0]}]]
      (is (pos? (metrics/pressing-intensity ff))))))
