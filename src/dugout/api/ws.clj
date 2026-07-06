(ns dugout.api.ws
  "WebSocket handler for live formation-stream metrics.
   Streams compactness and pressing intensity to the
   Live Tracking Monitor view."
  (:require [clojure.core.async :as a]
            [environ.core :refer [env]]
            [cheshire.core :as json]
            [ring.websocket :as ws]
            [dugout.auth :as auth]
            [taoensso.timbre :as log]))

(defn allowed-origin? [origin]
  (let [allowed (env :allowed-origin "http://localhost:3000")]
    (= origin allowed)))

(defn ws-listener
  "Returns a ring.websocket listener map that streams synthetic tracking metrics."
  [_request token]
  (let [authenticated? (some? (auth/unsign-token token))]
    {::ws/listener
     {:on-open
      (fn [socket]
        (if-not authenticated?
          (do
            (log/warn "Rejected unauthenticated WebSocket connection")
            (ws/close socket 4001 "Unauthorized"))
          (do
            (log/info "WebSocket connection established")
            (a/go-loop []
              (if (ws/open? socket)
                (do
                  (try
                    (let [msg (json/generate-string
                               {:type :tracking
                                :timestamp (System/currentTimeMillis)
                                :metrics {:compactness {:length (+ 30.0 (rand 10))
                                                        :width (+ 40.0 (rand 5))
                                                        :ratio (+ 25.0 (rand 5))}
                                          :pressing {:nearest3 (+ 7.0 (rand 2))
                                                     :ppda (+ 11.0 (rand 3))
                                                     :trigger (+ 5.0 (rand 2))}}})]
                      (ws/send socket msg))
                    (catch Exception e
                      (log/error "Error sending WS message" e)))
                  (a/<! (a/timeout (long 1000)))
                  (recur))
                (log/info "WebSocket socket closed, stopping emission loop"))))))
      :on-message
      (fn [_socket message]
        (log/info "Received message:" message))
      :on-close
      (fn [_socket code reason]
        (log/info "WebSocket closed:" code reason))
      :on-error
      (fn [_socket error]
        (log/error "WebSocket error:" error))}}))
