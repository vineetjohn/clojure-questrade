(ns clojure-questrade.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))


(def refresh-token-file-path ".refresh-token.json")
(def positions-api-endpoint "%s/v1/accounts/%s/positions")

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

(defn get-positions-endpoint
  "Get Questrade positions api endpoint"
  [api-url, account-id]
  (format positions-api-endpoint api-url account-id))

(defn get-positions
  "Get Questrade positions"
  [api-url, account-id, access-token]
  (client/get (get-positions-endpoint api-url account-id)
              {:oauth-token access-token}))

(defn read-refresh-token
  "Reads the current refresh toke from a file"
  []
  (def file-contents (json/read-str (slurp refresh-token-file-path)))
  (get file-contents "key"))
