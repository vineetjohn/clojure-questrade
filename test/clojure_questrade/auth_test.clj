(ns clojure-questrade.auth-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.auth :refer :all]))


(deftest get-tokens-test
  (with-redefs [refresh-token-api-endpoint "https://example.com"]
    (def response
      (get-tokens "qwerty"))
    (testing (is (= 200 (get response :status))))))
