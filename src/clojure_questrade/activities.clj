(ns clojure-questrade.activities
  (:require [clj-http.client :as client]
            [clojure.tools.logging :as log]))

(def activities-api-endpoint (str "%s/v1/accounts/%s/activities?"
                                  "startTime=%s&"
                                  "endTime=%s&"))

(defn get-activities-endpoint
  "Get Questrade activities api endpoint"
  [api-url, account-id, start, end]
  (format activities-api-endpoint api-url account-id
          start end))

(defn call-activities-api
  "Get Questrade activities"
  [api-url, account-id, access-token, start, end]
  (client/get (get-activities-endpoint api-url account-id start, end)
              {:oauth-token access-token}))
