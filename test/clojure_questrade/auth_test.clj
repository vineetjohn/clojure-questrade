(ns clojure-questrade.auth-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.auth :refer :all]))


(deftest get-token-endpoint-test
  (def endpoint (get-token-endpoint "123456"))
  (testing (is (= (str "https://login.questrade.com/oauth2/token"
                       "?grant_type=refresh_token"
                       "&refresh_token=123456")
                  endpoint))))

(deftest get-tokens-test
  (def response
    (get-tokens "https://example.com"
                "qwerty"))
  (testing (is (= 200 (get response :status)))))
