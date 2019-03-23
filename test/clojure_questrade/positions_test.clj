(ns clojure-questrade.positions-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.positions :refer :all]))

(deftest get-positions-endpoint-test
  (def endpoint (get-positions-endpoint "https://example.com" "123456"))
  (testing (is (= "https://example.com/v1/accounts/123456/positions" endpoint))))

(deftest get-positions-test
                ; mocking the method 'get-positions-endpoint'
  (with-redefs [get-positions-endpoint
                (fn [api-url, account-id] "https://example.com")]
    (def response
      (get-positions "https://example.com"
                     "123456"
                     "qwerty"))
    (testing (is (= 200 (get response :status))))))
