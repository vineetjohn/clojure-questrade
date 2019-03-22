(ns clojure-questrade.core
  (:require [clj-http.client :as client]))


(defn example-get
  "Example GET request"
  []
  (client/get "http://example.com"))
