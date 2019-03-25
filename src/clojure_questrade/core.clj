(ns clojure-questrade.core
  (:require [cheshire.core :as ches]
            [clojure.tools.logging :as log]
            [clojure-questrade.auth :as auth]
            [clojure-questrade.positions :as positions]))

; Constants

(def auth-tokens-file-path ".auth-tokens.json")


; JSON I/O

(defn read-auth-tokens
  "Reads the current refresh toke from a file"
  []
  (ches/parse-stream (clojure.java.io/reader auth-tokens-file-path) true))

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
      (auth/get-auth-response (get (read-auth-tokens) :refresh_token)))
    (log/info "auth-response" auth-response)
    (def auth-tokens
      (parse-tokens (get auth-response :body)))
    (log/info "auth-tokens" auth-tokens)
    (save-auth-tokens auth-tokens)
    (log/info "Credentials updated")
    (catch Exception e
      (log/error (str "Unable to get save refresh token: "
                      (.getMessage e)))
      (throw e))))


; Main

(defn -main
  "Program entry point"
  [& args]
  (log/info "Starting program execution")
  (update-credentials)
  (def auth-tokens (read-auth-tokens))
  (def access-token (get auth-tokens :access_token))
  (def api-server (get auth-tokens :api_server))
  (def acc-positions
    (get (positions/get-positions api-server
                                  "123456"
                                  access-token) :body))
  (log/info acc-positions)
  (log/info "Completed program execution"))
