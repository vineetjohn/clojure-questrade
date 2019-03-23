(ns clojure-questrade.core-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.core :refer :all]
            [clojure.string :as string]))


(deftest example-get-test
  (def response
    (example-get "http://example.com"))
  (testing (is (= 200 (get response :status)))))

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

(deftest read-refresh-token-test
  (def contents (read-refresh-token))
  (testing (is (not (string/blank? contents)))))
