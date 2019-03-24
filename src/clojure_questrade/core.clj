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
  (def response (auth/get-auth-response (read-refresh-token)))
  (log/info response)
  (def auth-object (auth/parse-tokens response))
  (log/info auth-object)
  (def save-refresh-token {:key (get auth-object :refresh_token)})
  (log/info "Completed program execution"))
