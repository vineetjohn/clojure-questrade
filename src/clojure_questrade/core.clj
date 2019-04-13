(ns clojure-questrade.core
  (:require [cheshire.core :as ches]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [java-time :as jtime]
            [clojure-questrade.auth :as auth]
            [clojure-questrade.activities :as activities]
            [clojure-questrade.positions :as positions]))


; Command line options
(def cli-options
  ;; An option with a required argument
  [["-a" "--account_name ACCOUNT_NAME" "Account identifier"
    :default "margin"
    :parse-fn #(identity %)
    :validate [#(and (not (nil? %)) (not (empty? %)))
               "Account identifier is needed"]]
   ["-h" "--help"]])


; Constants
(def auth-tokens-file-path ".auth-tokens.json")
(def accounts-file-path ".accounts.json")


; Structs
(def trade (create-struct :symbol :net-amount :date :quantity))
(def date-range (create-struct :start :end))


; JSON I/O
(defn read-json-with-keys
  "Reads the current refresh toke from a file"
  [file-path]
  (ches/parse-stream (clojure.java.io/reader file-path) true))

(defn parse-tokens
  "Parse needed tokens from auth response"
  [auth-response-body]
  (select-keys (ches/parse-string auth-response-body true)
               [:refresh_token :access_token :api_server]))

(defn save-auth-tokens
  "Saves the new tokens to a file"
  [content]
  (ches/generate-stream content
                        (clojure.java.io/writer auth-tokens-file-path)
                        {:pretty true}))


; Helper methods
(defn update-credentials
  "Update the access and refresh tokens used to access API"
  []
  (try
    (def auth-response
      (auth/get-auth-response
       (get (read-json-with-keys auth-tokens-file-path)
            :refresh_token)))
    (log/info "auth-response" auth-response)
    (def auth-tokens
      (parse-tokens (get auth-response :body)))
    (log/info "auth-tokens" auth-tokens)
    (save-auth-tokens auth-tokens)
    (log/info "Credentials updated")
    (catch Exception e
      (log/error (str "Unable to save refresh token: "
                      (.getMessage e)))
      (throw e))))

(defn is-trade
  "Check if an activity is a trade or not"
  [activity]
  (def action (get activity "action"))
  (or (= action "Buy") (= action "Sell")))

(defn convert-activity-to-trade
  "Convert an activity to a minimal trade information structure"
  [activity]
  (struct trade
          (get activity "symbol")
          (get activity "netAmount")
          (get activity "settlementDate")
          (get activity "quantity")))


(defn get-activities
  "Gets the account activities for a particular date range"
  [api-url, account-id, access-token, date-range]
  (def acc-activities-body
    (get (activities/call-activities-api
          api-url account-id access-token
          (get date-range :start)
          (get date-range :end)) :body))
  (get (ches/parse-string acc-activities-body) "activities"))

(defn calculate-acb
  "Calculate the adjusted cost base given an account and the date ranges"
  [account-id, date-ranges]
  (def auth-tokens (read-json-with-keys auth-tokens-file-path))
  (def access-token (get auth-tokens :access_token))
  (def api-server (get auth-tokens :api_server))
  (def activities
    (reduce concat ()
            (map (fn [x]
                   (get-activities api-server account-id
                                   access-token x))
                 date-ranges)))
  (log/info activities)
  (log/info (str "Total of " (count activities) " activities"))
  (def trades
    (map convert-activity-to-trade
         (filter is-trade activities)))
  (log/info trades)
  (log/info (str "Total of " (count trades) " trades"))
  (log/info "Completed ACB calculation"))


(defn format-date
  "Formats dates according to the ISO 8601 standard with Zulu time,
  rounded down to the zeroth hour"
  [date]
  (jtime/format "yyyy-MM-dd'T'00:00:00'Z'" date))


(defn get-date-range
  "Constructs a date range structure and stringifies the dates"
  [start, end]
  (struct date-range
          (format-date start)
          (format-date end)))


(defn get-date-ranges
  "Get the date ranges to retrieve activities for"
  [start]
  (def now (jtime/zoned-date-time))
  (def end (java-time/plus start (java-time/days 30)))
  (if (= (jtime/max end now) end)
    (list (get-date-range start now))
    (list* (get-date-range start end) (get-date-ranges end))))


; Main
(defn -main
  "Program entry point"
  [& args]
  (log/info "Starting program execution")
  (def parsed-options (get (cli/parse-opts args cli-options) :options))
  (def account
    (get (read-json-with-keys accounts-file-path)
         (keyword (get parsed-options :account_name))))
  (def account-id (get account :id))
  (def account-start
    (jtime/zoned-date-time "yyyy-MM-dd HH:mm:ss VV"
                           (get account :start-date)))
  (log/info (str account-id ", " account-start))
  (def date-ranges (get-date-ranges account-start))
  (log/info date-ranges)
  (update-credentials)
  (calculate-acb account-id date-ranges)
  (log/info "Completed program execution"))
