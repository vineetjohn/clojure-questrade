(ns clojure-questrade.core-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.core :refer :all]))

(deftest a-test
  (testing (iden "abc")
    (is (= "abc"))))
