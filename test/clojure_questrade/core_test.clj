(ns clojure-questrade.core-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.core :refer :all]
            [clojure.string :as string]
            [cheshire.core :as ches]))


(deftest get-token-endpoint-test
  (def endpoint (get-token-endpoint "123456"))
  (testing (is (= (str "https://login.questrade.com/oauth2/token"
                       "?grant_type=refresh_token"
                       "&refresh_token=123456")
                  endpoint))))


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
  (testing (is (not (string/blank? (read-refresh-token))))))

(deftest save-refresh-token-test
  (save-refresh-token {:key "xyz"})
  (testing (is (= (get (ches/parse-stream (clojure.java.io/reader
                                           refresh-token-file-path))
                       "key")
                  "xyz"))))
