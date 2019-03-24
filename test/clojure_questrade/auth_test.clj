(ns clojure-questrade.auth-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.auth :refer :all]
            [clj-http.client :as client]))


(def test-response {:status 200
                    :body {:access_token "abc"
                           :api_server "https://example.com/"
                           :expires_in 10
                           :refresh_token "xyz"
                           :token_type "Bearer"}})

(deftest get-auth-response-test
  (with-redefs [client/get (fn [url, query_params] test-response)]
    (def response (get-auth-response "qwerty"))
    (def response-status (get response :status))
    (is (= 200 response-status))
    (def response-body (get response :body))
    (is (= (get response-body :refresh_token) "xyz"))
    (is (= (get response-body :access_token) "abc"))
    (is (= (get response-body :api_server) "https://example.com/"))))
