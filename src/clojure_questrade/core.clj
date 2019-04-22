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
   ["-y" "--tax_year TAX_YEAR" "Tax year"
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 2017 %)
               "Tax year cannot be less than 2018"]]
   ["-h" "--help"]])


; Constants
(def auth-tokens-file-path ".auth-tokens.json")
(def accounts-file-path ".accounts.json")


; Structs
(def trade (create-struct :symbol :action :net-amount :date :quantity))
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
    (def auth-tokens
      (parse-tokens (get auth-response :body)))
    (save-auth-tokens auth-tokens)
    (log/info "Credentials updated")
    (catch Exception e
      (log/error (str "Unable to save refresh token: "
                      (.getMessage e)))
      (throw e))))

(defn is-trade-for-tax-year
  "Check if an activity is a trade or not in the required tax year"
  [activity, tax-year]
  (def action (get activity "action"))
  (def date-str (get activity "settlementDate"))
  (or (= action "Buy")
      (and (= action "Sell")
           (= (subs date-str 0 4) (str tax-year)))))

(defn convert-activity-to-trade
  "Convert an activity to a minimal trade information structure"
  [activity]
  (struct trade
          (get activity "symbol")
          (get activity "action")
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

(defn calc-cap-gains-for-symbol
  "Calculate ACB for a given symbol"
  [trades, total-shares, total-cost, total-gains]
  (if (empty? trades)
    total-gains
    (do (def trade (first trades))
        (def action (get trade :action))
        (def shares (get trade :quantity))
        (def amount (get trade :net-amount))
        (if (= action "Buy")
          (calc-cap-gains-for-symbol (rest trades)
                                     (+ total-shares shares)
                                     (+ total-cost amount)
                                     total-gains)
          (do (def acb (/ total-cost total-shares))
              (def sale-price (/ amount shares))
              (def gain (* (- sale-price acb) shares))
              (calc-cap-gains-for-symbol (rest trades)
                                         (- total-shares shares)
                                         (- total-cost (* acb shares))
                                         (+ total-gains gain)))))))

(defn get-symbol-trades
  "Transform trades into a map of symbols to trades"
  [trades, symbol-trades]
  (if (empty? trades)
    symbol-trades
    (do
      (def trade (first trades))
      (def trade-symbol (get trade :symbol))
      (def prev-trades (get symbol-trades trade-symbol))
      (def new-symbol-trades
        (assoc symbol-trades
               trade-symbol
               (if (nil? prev-trades)
                 (vector trade)
                 (conj prev-trades trade))))
      (get-symbol-trades (rest trades)
                         new-symbol-trades))))

(defn calc-cap-gains
  "Calculate the adjusted cost base given an account and the date ranges"
  [account-id, date-ranges, tax-year]
  ; Read auth and api details
  (def auth-tokens (read-json-with-keys auth-tokens-file-path))
  (def access-token (get auth-tokens :access_token))
  (def api-server (get auth-tokens :api_server))

  ; Make calls to Questrade API
  (def activities
    (reduce concat ()
            (map (fn [x]
                   (get-activities api-server account-id
                                   access-token x))
                 date-ranges)))
  (log/info (str "Total of " (count activities) " activities"))

  ; Parse activities into trades
  (def trades
    (map convert-activity-to-trade
         (filter (fn [x] (is-trade-for-tax-year x tax-year))
                 activities)))
  (log/info (str "Total of " (count trades) " trades"))

  (def symbol-trades
    (get-symbol-trades (apply list trades) (hash-map)))

  ; For each group of symbol trades, calculate capital gains
  (def symbol-capital-gains
    (map
     (fn [x] (calc-cap-gains-for-symbol (sort-by :date (second x))
                                        0 0 0))
     (seq symbol-trades)))

  ; Aggregate the capital gains
  (double (reduce + 0 symbol-capital-gains)))


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
  (def tax-year (get parsed-options :tax_year))
  (def account
    (get (read-json-with-keys accounts-file-path)
         (keyword (get parsed-options :account_name))))
  (def account-id (get account :id))
  (def account-start
    (jtime/zoned-date-time "yyyy-MM-dd HH:mm:ss VV"
                           (get account :start-date)))
  (log/info (str "Account ID: " account-id ", Start Date: " account-start))
  (log/info (str "Tax year: " tax-year))
  (def date-ranges (get-date-ranges account-start))
  (update-credentials)
  (def cap-gains (calc-cap-gains account-id date-ranges tax-year))
  (log/info (str "Overall capital gains for " tax-year ":"
                 " $" (format "%.2f" cap-gains)))
  (log/info "Completed program execution"))
