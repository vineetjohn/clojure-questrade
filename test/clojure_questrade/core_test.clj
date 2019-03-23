(ns clojure-questrade.core-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.core :refer :all]
            [clojure.string :as string]))


(deftest example-get-test
  (def response
    (example-get "http://example.com"))
  (testing (is (= 200 (get response :status)))))

(deftest get-balances-test
  (def response
    (get-balances "https://example.com"
                  "qwerty"))
  (testing (is (= 200 (get response :status)))))

(deftest read-refresh-token-test
  (def contents (read-refresh-token))
  (testing (is (not (string/blank? contents)))))
