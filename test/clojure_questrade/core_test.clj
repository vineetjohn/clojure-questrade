(ns clojure-questrade.core-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.core :refer :all]))

(deftest a-test
  (testing (iden "abc")
    (is (= "abc"))))

(deftest example-get-test
  (testing (get (example-get) :status)
    (is (= 200))))
