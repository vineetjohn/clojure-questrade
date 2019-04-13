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


; Helpers
(defn update-credentials
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
  [activity]
  (def action (get activity "action"))
  (or (= action "Buy") (= action "Sell")))

(defn convert-activity-to-trade
  [activity]
  (struct trade
          (get activity "symbol")
          (get activity "netAmount")
          (get activity "settlementDate")
          (get activity "quantity")))


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
  (log/info account-id)
  (update-credentials)
  (def auth-tokens (read-json-with-keys auth-tokens-file-path))
  (def access-token (get auth-tokens :access_token))
  (def api-server (get auth-tokens :api_server))
  (def acc-activities-body
    (get (activities/get-activities api-server
                                    account-id
                                    access-token) :body))
  (def acc-activities
    (get (ches/parse-string acc-activities-body) "activities"))
  (log/info acc-activities)
  (def trades
    (map convert-activity-to-trade
         (filter is-trade acc-activities)))
  (log/info trades)
  (log/info "Completed program execution"))
