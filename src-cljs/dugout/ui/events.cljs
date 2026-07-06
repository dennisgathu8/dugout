(ns dugout.ui.events
  (:require [re-frame.core :as rf]))

;; Initialisation
(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:active-view    :dashboard
    :squad          []
    :rules          []
    :player-history []
    :intl-break     []
    :tracking-live  []
    :token          nil
    :loading        false
    :error          nil}))

;; Navigation and WebSocket lifecycle hook
(rf/reg-event-fx
 :set-view
 (fn [{:keys [db]} [_ view]]
   (let [old-view (:active-view db)
         new-db   (assoc db :active-view view)
         ;; Determine if we need to open or close WebSocket
         effects (cond-> {:db new-db}
                   (and (= view :tracking) (:token db))
                   (assoc :ws-connect {:url (str "/ws/tracking?token=" (:token db))
                                       :on-message [:live-tracking-received]})
                   
                   (and (not= view :tracking) (= old-view :tracking))
                   (assoc :ws-disconnect nil)
                   
                   (= view :player)
                   (assoc :dispatch [:load-player-history :p101])
                   
                   (= view :intl-break)
                   (assoc :dispatch [:load-intl-break]))]
     effects)))

;; Auth
(rf/reg-event-fx
 :login-demo
 (fn [{:keys [db]} _]
   {:db       (assoc db :loading true)
    :http-get {:url      "/api/auth/login"
               :method   :post
               :on-success [:login-success]
               :on-failure [:api-error]}}))

(rf/reg-event-fx
 :login-success
 (fn [{:keys [db]} [_ {:keys [token]}]]
   (let [new-db (assoc db :token token :loading false)
         active-view (:active-view new-db)
         effects (cond-> {:db new-db}
                   (= active-view :tracking)
                   (assoc :ws-connect {:url (str "/ws/tracking?token=" token)
                                       :on-message [:live-tracking-received]}))]
     effects)))

;; Squad data
(rf/reg-event-fx
 :load-squad
 (fn [{:keys [db]} _]
   {:db       (assoc db :loading true)
    :http-get {:url      "/api/squad"
               :token    (:token db)
               :on-success [:squad-loaded]
               :on-failure [:api-error]}}))

(rf/reg-event-db
 :squad-loaded
 (fn [db [_ {:keys [players]}]]
   (-> db
       (assoc :squad players :loading false))))

;; Player history
(rf/reg-event-fx
 :load-player-history
 (fn [{:keys [db]} [_ player-id]]
   {:db       (assoc db :loading true)
    :http-get {:url      (str "/api/player/" (name player-id) "/history")
               :token    (:token db)
               :on-success [:player-history-loaded]
               :on-failure [:api-error]}}))

(rf/reg-event-db
 :player-history-loaded
 (fn [db [_ {:keys [history]}]]
   (-> db
       (assoc :player-history history :loading false))))

;; International break impact data
(rf/reg-event-fx
 :load-intl-break
 (fn [{:keys [db]} _]
   {:db       (assoc db :loading true)
    :http-get {:url      "/api/intl-break"
               :token    (:token db)
               :on-success [:intl-break-loaded]
               :on-failure [:api-error]}}))

(rf/reg-event-db
 :intl-break-loaded
 (fn [db [_ {:keys [impact]}]]
   (-> db
       (assoc :intl-break impact :loading false))))

;; Live tracking WebSocket data
(rf/reg-event-db
 :live-tracking-received
 (fn [db [_ msg]]
   (assoc db :tracking-live (:metrics msg))))

;; Demo mode — load without auth
(rf/reg-event-fx
 :load-demo
 (fn [{:keys [db]} _]
   {:db       (assoc db :loading true)
    :http-get {:url      "/demo"
               :on-success [:demo-loaded]
               :on-failure [:api-error]}}))

(rf/reg-event-fx
 :demo-loaded
 (fn [{:keys [db]} [_ {:keys [squad rules]}]]
   {:db (-> db
            (assoc :squad squad
                   :rules rules
                   :loading false))
    :dispatch [:login-demo]}))

(rf/reg-event-db
 :api-error
 (fn [db [_ error]]
   (-> db
       (assoc :error (str error) :loading false))))

(rf/reg-event-db
 :update-rule-threshold
 (fn [db [_ rule-id threshold]]
   (update db :rules
           (fn [rules]
             (mapv (fn [r]
                     (if (= (:rule/id r) rule-id)
                       (assoc r :rule/fatigue-threshold threshold)
                       r))
                   rules)))))
