(ns clojure-questrade.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))


(def refresh-token-file-path ".refresh-token.json")
(def refresh-token-api-endpoint
  (str "https://login.questrade.com/oauth2/token"
       "?grant_type=refresh_token"
       "&refresh_token=%s"))
(def positions-api-endpoint "%s/v1/accounts/%s/positions")

(defn get-token-endpoint
  "Get Questrade endpoint to get tokens"
  [refresh-token]
  (format refresh-token-api-endpoint refresh-token))

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
