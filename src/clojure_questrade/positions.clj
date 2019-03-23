(ns clojure-questrade.positions
  (:require [clj-http.client :as client]
            [clojure.tools.logging :as log]))

(def positions-api-endpoint "%s/v1/accounts/%s/positions")

(defn get-positions-endpoint
  "Get Questrade positions api endpoint"
  [api-url, account-id]
  (format positions-api-endpoint api-url account-id))

(defn get-positions
  "Get Questrade positions"
  [api-url, account-id, access-token]
  (client/get (get-positions-endpoint api-url account-id)
              {:oauth-token access-token}))
