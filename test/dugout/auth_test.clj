(ns dugout.auth-test
  (:require [clojure.test :refer [deftest is testing]]
            [dugout.auth :as auth]))

(deftest test-sign-and-unsign
  (testing "Sign and unsign round-trip"
    (let [claims {:user "test" :role :demo}
          token  (auth/sign-token claims)
          result (auth/unsign-token token)]
      (is (some? result))
      (is (= "test" (:user result))))))

(deftest test-demo-token
  (testing "Demo token is valid"
    (let [token  (auth/demo-token)
          result (auth/unsign-token token)]
      (is (some? result))
      (is (= "demo" (:user result)))
      (is (some? (:exp result))))))

(deftest test-invalid-token
  (testing "Invalid token returns nil"
    (is (nil? (auth/unsign-token "invalid-token-value")))))
