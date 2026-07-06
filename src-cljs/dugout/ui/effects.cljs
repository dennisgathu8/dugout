(ns dugout.ui.effects
  "HTTP and WebSocket side-effect handlers for re-frame.
   Registers :http-get effect for API calls."
  (:require [re-frame.core :as rf]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(rf/reg-fx
 :http-get
 (fn [{:keys [url method token on-success on-failure]}]
   (go
     (let [opts    (cond-> {}
                     token (assoc-in [:headers "Authorization"]
                                     (str "Token " token)))
           response (case (or method :get)
                      :get  (<! (http/get url opts))
                      :post (<! (http/post url opts))
                      :put  (<! (http/put url opts)))]
       (if (:success response)
         (rf/dispatch (conj on-success (:body response)))
         (rf/dispatch (conj on-failure
                            (or (:error-text response)
                                "Request failed"))))))))

(defonce ws-connection (atom nil))

(rf/reg-fx
 :ws-connect
 (fn [{:keys [url on-message]}]
   (when-not @ws-connection
     (let [host (.. js/window -location -host)
           protocol (if (= (.. js/window -location -protocol) "https:") "wss:" "ws:")
           ws-url (str protocol "//" host url)
           ws (js/WebSocket. ws-url)]
       (set! (.-onmessage ws)
             (fn [event]
               (let [data (js/JSON.parse (.-data event))
                     clj-data (js->clj data :keywordize-keys true)]
                 (rf/dispatch (conj on-message clj-data)))))
       (reset! ws-connection ws)))))

(rf/reg-fx
 :ws-disconnect
 (fn [_]
   (when-let [ws @ws-connection]
     (.close ws)
     (reset! ws-connection nil))))
