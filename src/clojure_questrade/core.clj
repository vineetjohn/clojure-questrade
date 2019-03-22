(ns clojure-questrade.core
  (:require [clj-http.client :as client]))


(defn example-get
  "Example GET request"
  [url]
  (client/get url))

(defn get-access-token
  "Get Questrade access token"
  [api-url, refresh-token]
  (client/get api-url
              {:query-params {:grant_type "refresh_token"
                              :refresh_token refresh-token}}))

(defn get-balances
  "Get Questrade balances"
  [api-url, access-token]
  (client/get api-url
              {:oauth-token access-token}))
