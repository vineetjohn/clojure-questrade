(ns clojure-questrade.auth
  (:require [clj-http.client :as client]
            [clojure.tools.logging :as log]))

(def refresh-token-api-endpoint "https://login.questrade.com/oauth2/token")

(defn get-auth-response
  "Get new Questrade tokens and API URL"
  [refresh-token]
  (client/get refresh-token-api-endpoint
              {:query-params {:grant_type "refresh_token"
                              :refresh_token refresh-token}}))

(defn parse-tokens
  "Parse needed tokens from auth response"
  [auth-response]
  (select-keys auth-response
               [:refresh_token :access_token :api_server]))
