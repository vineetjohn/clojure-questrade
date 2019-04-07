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

(deftest save-auth-tokens-test
  (save-auth-tokens test-auth-tokens)
  (def persisted-auth-tokens
    (ches/parse-stream (clojure.java.io/reader auth-tokens-file-path) true))
  (is (= persisted-auth-tokens test-auth-tokens)))

(deftest parse-tokens-test
  (def tokens (parse-tokens test-response-body))
  (is (= (get tokens :refresh_token) "xyz"))
  (is (= (get tokens :access_token) "abc"))
  (is (= (get tokens :api_server) "https://example.com/")))
