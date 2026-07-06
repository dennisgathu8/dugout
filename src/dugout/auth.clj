(ns dugout.auth
  "JWT authentication via buddy-auth.
   The JWT secret comes from JWT_SECRET env var only.
   Never hardcoded."
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication
                                           wrap-authorization]]
            [buddy.sign.jwt :as jwt]
            [environ.core :refer [env]]))

(defn- jwt-secret []
  (or (env :jwt-secret)
      (throw (ex-info "JWT_SECRET not set"
                      {:type :dugout/config-error}))))

(defn sign-token
  "Creates a signed JWT for a demo or authenticated user."
  [claims]
  (jwt/sign claims (jwt-secret) {:alg :hs256}))

(defn unsign-token [token]
  (try
    (jwt/unsign token (jwt-secret) {:alg :hs256})
    (catch Exception _ nil)))

(defn unauthorized-handler [_request err]
  {:status 401
   :body {:error "Unauthorized"
          :message (or (and err (ex-message err)) "Invalid or missing token")}})

(def backend
  (jws-backend {:secret (fn [_] (jwt-secret))
                :unauthorized-handler unauthorized-handler
                :options {:alg :hs256}}))

(defn wrap-auth [handler]
  (-> handler
      (wrap-authentication backend)
      (wrap-authorization backend)))

(defn require-auth [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      {:status 401
       :body   {:error "Authentication required"}})))

(defn demo-token
  "Generates a short-lived demo token for the public demo."
  []
  (sign-token {:user "demo"
               :role :demo
               :exp  (+ (quot (System/currentTimeMillis) 1000)
                        (* 60 60 24))}))
