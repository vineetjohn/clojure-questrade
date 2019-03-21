(ns clojure-questrade.core
  (:require [clj-http.client :as client]))

(defn iden
  "I don't do a whole lot."
  [x]
  x)


(defn example-get
  "Example GET request"
  []
  (client/get "http://example.com"))
