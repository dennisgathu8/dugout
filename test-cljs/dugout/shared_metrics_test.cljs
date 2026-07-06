(ns dugout.shared-metrics-test
  (:require [cljs.test :refer [deftest is testing]]
            [dugout.shared.metrics :as metrics]
            [dugout.shared.zones   :as zones]))

;; IDENTICAL assertions to the JVM .cljc test file.
;; Same functions. Same inputs. Same expected outputs.
;; This is the .cljc architectural proof.

(deftest test-compute-zone-cljs
  (testing "Same zone computation in browser runtime"
    (is (= :zone10 (zones/compute-zone [60.0 40.0])))
    (is (= :zone01 (zones/compute-zone [5.0 5.0])))
    (is (= :zone16 (zones/compute-zone [110.0 40.0])))))

(def test-xg-fixture-value 0.23402207538578235)
(def test-xg-pressured-fixture-value 0.19891876407791498)

(deftest test-estimate-xg-cljs
  (testing "Same xG in browser runtime — .cljc proof"
    (let [xg (metrics/estimate-xg
              {:type {:name "Shot"}
               :location [95.0 30.0]})
          pressured (metrics/estimate-xg
                     {:type {:name "Shot"}
                      :location [95.0 30.0]
                      :under_pressure true})]
      (is (= test-xg-fixture-value xg))
      (is (= test-xg-pressured-fixture-value pressured)))))

(deftest test-euclidean-dist-cljs
  (testing "Same distance in browser runtime"
    (is (= 5.0 (metrics/euclidean-dist [0.0 0.0] [3.0 4.0])))))

(deftest test-pressing-intensity-cljs
  (testing "Same pressing intensity in browser runtime"
    (let [ff [{:actor true :teammate true :location [65.0 40.0]}
              {:actor false :teammate false :location [55.0 35.0]}
              {:actor false :teammate false :location [58.0 42.0]}
              {:actor false :teammate false :location [52.0 38.0]}]]
      (is (pos? (metrics/pressing-intensity ff))))))
