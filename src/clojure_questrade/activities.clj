(ns clojure-questrade.activities
  (:require [clj-http.client :as client]
            [clojure.tools.logging :as log]))

(def activities-api-endpoint (str "%s/v1/accounts/%s/activities?"
                                  "startTime=%s&"
                                  "endTime=%s&"))

(defn get-activities-endpoint
  "Get Questrade activities api endpoint"
  ; [api-url, account-id, start-time, end-time]
  [api-url, account-id]
  (format activities-api-endpoint api-url account-id
          "2019-01-01T00:00:00Z" "2019-01-31T00:00:00Z"))

(defn get-activities
  "Get Questrade activities"
  [api-url, account-id, access-token]
  (client/get (get-activities-endpoint api-url account-id)
              {:oauth-token access-token}))
