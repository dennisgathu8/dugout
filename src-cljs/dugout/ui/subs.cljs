(ns dugout.ui.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub :active-view    (fn [db _] (:active-view db)))
(rf/reg-sub :squad          (fn [db _] (:squad db)))
(rf/reg-sub :rules          (fn [db _] (:rules db)))
(rf/reg-sub :loading        (fn [db _] (:loading db)))
(rf/reg-sub :error          (fn [db _] (:error db)))
(rf/reg-sub :tracking-live  (fn [db _] (:tracking-live db)))
(rf/reg-sub :intl-break     (fn [db _] (:intl-break db)))
(rf/reg-sub :player-history (fn [db _] (:player-history db)))


(rf/reg-sub
 :fatigue-high-risk
 :<- [:squad]
 (fn [squad _]
   (filter #(> (:fatigue % 0) 0.75) squad)))
