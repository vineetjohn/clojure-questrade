(ns clojure-questrade.core-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.core :refer :all]))


(deftest example-get-test
  (def response
    (example-get "http://example.com"))
  (testing (get response :status)
    (is (= 200))))

(deftest get-balances-test
  (def response
    (get-balances "https://example.com"
                  "qwerty"))
  (testing (get response :status)
    (is (= 200))))
