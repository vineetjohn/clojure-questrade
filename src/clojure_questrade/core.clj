(ns clojure-questrade.core
  (:require [cheshire.core :as ches]
            [clojure.tools.logging :as log]
            [clojure-questrade.auth :as auth]))

; Constants

(def refresh-token-file-path ".refresh-token.json")


; JSON I/O

(defn read-refresh-token
  "Reads the current refresh toke from a file"
  []
  (get (ches/parse-stream (clojure.java.io/reader refresh-token-file-path))
       "key"))

(defn parse-tokens
  "Parse needed tokens from auth response"
  [auth-response-body]
  (select-keys (ches/parse-string auth-response-body true)
               [:refresh_token :access_token :api_server]))

(defn save-refresh-token
  "Saves the new refresh toke to a file"
  [content]
  (ches/generate-stream content
                        (clojure.java.io/writer refresh-token-file-path)
                        {:pretty true}))


; Main

(defn -main
  "Program entry point"
  [& args]
  (log/info "Starting program execution")
  (try
    (def response (auth/get-auth-response (read-refresh-token)))
    (log/info response)
    (def auth-object (parse-tokens (get response :body)))
    (log/info auth-object)
    (save-refresh-token {:key (get auth-object :refresh_token)})
    (catch Exception e
      (log/error (str "Unable to get save refresh token: "
                      (.getMessage e)))))
  (log/info "Completed program execution"))
