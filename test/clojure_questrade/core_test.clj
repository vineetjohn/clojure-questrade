(ns clojure-questrade.core-test
  (:require [clojure.test :refer :all]
            [clojure-questrade.core :refer :all]
            [clojure.string :as string]
            [cheshire.core :as ches]))


(deftest read-refresh-token-test
  (testing (is (not (string/blank? (read-refresh-token))))))

(deftest save-refresh-token-test
  (save-refresh-token {:key "xyz"})
  (testing (is (= (get (ches/parse-stream (clojure.java.io/reader
                                           refresh-token-file-path))
                       "key")
                  "xyz"))))
