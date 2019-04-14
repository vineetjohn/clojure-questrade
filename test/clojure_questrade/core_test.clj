(ns clojure-questrade.core-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.core :refer :all]
            [clojure.string :as string]
            [cheshire.core :as ches]))

(def test-response-body (str "{\"access_token\": \"abc\","
                             "\"api_server\": \"https://example.com/\","
                             "\"expires_in\": 10,"
                             "\"refresh_token\": \"xyz\","
                             "\"token_type\": \"Bearer\"}"))
(def test-auth-tokens {:access_token "abc-new"
                       :refresh_token "xyz-new"
                       :api_server "https://example.com/new"})

(deftest read-json-with-keys-test
  (def new-auth-tokens (read-json-with-keys auth-tokens-file-path))
  (is (not (nil? new-auth-tokens)))
  (is (not (empty? new-auth-tokens))))

(deftest parse-tokens-test
  (def tokens (parse-tokens test-response-body))
  (is (= (get tokens :refresh_token) "xyz"))
  (is (= (get tokens :access_token) "abc"))
  (is (= (get tokens :api_server) "https://example.com/")))

(deftest is-trade-for-tax-year-test
  (def mock-activity-1 {"action" "Buy", "settlementDate" "2018-01-01"})
  (def mock-activity-2 {"action" "Deposit", "settlementDate" "2019-01-01"})
  (def mock-activity-3 {"action" "Sell", "settlementDate" "2018-01-01"})
  (def mock-activity-4 {"action" "Sell", "settlementDate" "2019-01-01"})
  (def mock-tax-year "2019")
  (is (is-trade-for-tax-year mock-activity-1 mock-tax-year))
  (is (not (is-trade-for-tax-year mock-activity-2 mock-tax-year)))
  (is (not (is-trade-for-tax-year mock-activity-3 mock-tax-year)))
  (is (is-trade-for-tax-year mock-activity-4 mock-tax-year)))
