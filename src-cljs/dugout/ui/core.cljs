(ns dugout.ui.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [dugout.ui.events]
            [dugout.ui.subs]
            [dugout.ui.effects]
            [dugout.ui.views.dashboard  :as dashboard]
            [dugout.ui.views.squad      :as squad]
            [dugout.ui.views.pressing   :as pressing]
            [dugout.ui.views.player     :as player]
            [dugout.ui.views.intl-break :as intl-break]
            [dugout.ui.views.tracking   :as tracking]))

(defn nav []
  [:nav {:class "nav"}
   [:span {:style {:font-weight "bold" :color "#fff" :font-size "1.1rem"}} "⚽ dugout"]
   (for [[view label] [[:dashboard "Dashboard"]
                        [:squad "Squad Availability"]
                        [:pressing "Pressing Analysis"]
                        [:player "Player Timeline"]
                        [:intl-break "International Breaks"]
                        [:tracking "Live Tracking"]]]
     [:a {:key view :href "#"
          :style {:color (if (= @(rf/subscribe [:active-view]) view) "#fff" "#8FB8D4")}
          :on-click (fn [e] (.preventDefault e) (rf/dispatch [:set-view view]))}
      label])])

(defn main-view []
  (case @(rf/subscribe [:active-view])
    :squad      [squad/squad-view]
    :pressing   [pressing/pressing-view]
    :player     [player/player-view]
    :intl-break [intl-break/intl-break-view]
    :tracking   [tracking/tracking-view]
    [dashboard/dashboard-view]))

(defn app []
  [:div
   [nav]
   [:div {:id "app" :style {:padding "2rem"}}
    (when @(rf/subscribe [:loading])
      [:p {:style {:color "#8FB8D4"}} "Loading..."])
    (when-let [err @(rf/subscribe [:error])]
      [:div {:class "card" :style {:border-left "4px solid #8b1a1a"}}
       [:strong "Error: "] err])
    [main-view]]])

(defn init []
  (rf/dispatch-sync [:initialize])
  (rf/dispatch [:load-demo])
  (rdom/render [app] (js/document.getElementById "app")))

(defn reload []
  (rdom/render [app] (js/document.getElementById "app")))
