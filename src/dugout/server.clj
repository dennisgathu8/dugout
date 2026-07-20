(ns dugout.server
  "Ring server with all middleware and routes."
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response
                                           wrap-json-body]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.websocket :as rws]
            [ring.util.response :as response]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [dugout.auth   :as auth]
            [dugout.db     :as db]
            [dugout.api.player :as player-api]
            [dugout.api.rules  :as rules-api]
            [dugout.api.ws     :as ws]
            [environ.core :refer [env]]
            [taoensso.timbre :as log])
  (:gen-class))

(defn security-headers [handler]
  (fn [request]
    (let [response (handler request)]
      (update response :headers merge
              {"Content-Security-Policy"
               "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; connect-src 'self' ws: wss:"
               "X-Frame-Options" "DENY"
               "X-Content-Type-Options" "nosniff"
               "Referrer-Policy" "strict-origin-when-cross-origin"}))))

(defn parse-date [s]
  (when (seq s)
    (try
      (java.util.Date/from (java.time.Instant/parse s))
      (catch Exception _
        (try
          (java.util.Date/from (java.time.Instant/parse (str s "T00:00:00Z")))
          (catch Exception _ nil))))))

(defroutes public-routes
  (GET  "/"        [] (-> (response/resource-response "index.html" {:root "public"})
                          (response/content-type "text/html; charset=utf-8")))
  (POST "/api/auth/login" [] {:status 200
                               :body {:token (auth/demo-token)}})
  (GET  "/demo"    [] {:status 200
                        :body {:squad (player-api/synthetic-squad)
                               :rules (rules-api/get-rules)}})
  (GET "/ws/tracking" req
    (if (rws/upgrade-request? req)
      (let [origin (or (get-in req [:headers "origin"]) (get-in req [:headers "Origin"]))
            params (:params req)
            token (or (get params :token) (get params "token"))]
        (if-not (ws/allowed-origin? origin)
          (do (log/warn "Rejected WebSocket from unknown origin" {:origin origin})
              {:status 400 :body "Origin not allowed"})
          (ws/ws-listener req token)))
      {:status 400 :body "Not a websocket upgrade request"})))

(defroutes protected-routes
  (GET "/api/squad"  req
    (let [params (:params req)
          vt-param (or (get params :valid-time) (get params "valid-time"))
          tt-param (or (get params :tx-time) (get params "tx-time"))
          vt (or (parse-date vt-param) (java.util.Date.))
          tt (parse-date tt-param)]
      {:status 200
       :body {:players (player-api/available-players vt tt)}}))
  (GET "/api/rules"  _
    {:status 200 :body {:rules (rules-api/get-rules)}})
  (GET "/api/player/:id/history" [id]
    {:status 200
     :body {:history (player-api/player-history id)}})
  (GET "/api/intl-break" req
    (let [params (:params req)
          pre-param (or (get params :pre-break) (get params "pre-break"))
          post-param (or (get params :post-break) (get params "post-break"))
          pre (or (parse-date pre-param) #inst "2026-06-01T00:00:00Z")
          post (or (parse-date post-param) #inst "2026-06-15T00:00:00Z")]
      {:status 200
       :body {:impact (player-api/international-break-impact pre post)}})))

(defroutes app-routes
  (route/resources "/")
  public-routes
  (-> protected-routes auth/require-auth)
  (route/not-found {:status 404 :body {:error "Not found"}}))

(def app
  (-> app-routes
      auth/wrap-auth
      (wrap-json-body {:keywords? true})
      wrap-json-response
      wrap-params
      wrap-content-type
      security-headers))

(defn -main [& _]
  (log/info "Starting dugout server")
  (db/get-node)
  (run-jetty app {:port  (Integer/parseInt
                           (env :port "3000"))
                  :join? true}))
