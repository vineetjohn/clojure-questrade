(ns clojure-questrade.core
  (:require [clj-http.client :as client]))


(defn example-get
  "Example GET request"
  [url]
  (client/get url))

(defn get-balances
  "Get Questrade balances"
  [api-url, access-token]
  (client/get api-url
              {:oauth-token access-token}))
