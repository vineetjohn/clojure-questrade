(ns clojure-questrade.auth
  (:require [clj-http.client :as client]
            [clojure.tools.logging :as log]))

(def refresh-token-api-endpoint
  (str "https://login.questrade.com/oauth2/token"
       "?grant_type=refresh_token"
       "&refresh_token=%s"))

(defn get-token-endpoint
  "Get Questrade endpoint to get tokens"
  [refresh-token]
  (format refresh-token-api-endpoint refresh-token))

(defn get-tokens
  "Get new Questrade tokens and API URL"
  [auth-url, refresh-token]
  (client/get auth-url
              {:query-params {:grant_type "refresh_token"
                              :refresh_token refresh-token}}))
