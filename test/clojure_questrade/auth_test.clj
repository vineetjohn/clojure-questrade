(ns clojure-questrade.auth-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.auth :refer :all]
            [clj-http.client :as client]))


(def test-response {:status 200
                    :body ""})

(deftest get-auth-response-test
  (with-redefs [client/get (fn [url, query_params] test-response)]
    (def response (get-auth-response "qwerty"))
    (def response-status (get response :status))
    (is (= 200 response-status))))
