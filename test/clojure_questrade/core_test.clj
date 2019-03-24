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

(deftest read-refresh-token-test
  (testing (is (not (string/blank? (read-refresh-token))))))

(deftest save-refresh-token-test
  (save-refresh-token {:key "xyz"})
  (testing (is (= (get (ches/parse-stream (clojure.java.io/reader
                                           refresh-token-file-path))
                       "key")
                  "xyz"))))

(deftest parse-tokens-test
  (def tokens (parse-tokens test-response-body))
  (is (= (get tokens :refresh_token) "xyz"))
  (is (= (get tokens :access_token) "abc"))
  (is (= (get tokens :api_server) "https://example.com/")))
