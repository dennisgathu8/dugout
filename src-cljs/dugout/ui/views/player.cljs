(ns dugout.ui.views.player
  "Player Timeline view.
   Development trajectory over months and years for
   academy and club players. Shows fatigue, availability,
   and performance metrics over time."
  (:require [re-frame.core :as rf]))

(defn timeline-point [{:keys [date fatigue available event]}]
  [:div {:style {:display "flex" :align-items "center"
                 :gap "1rem" :padding "0.75rem 0"
                 :border-bottom "1px solid #2E6DA4"}}
   [:div {:style {:width 12 :height 12
                  :border-radius "50%"
                  :background (cond
                                (not available) "#8b1a1a"
                                (> fatigue 0.75) "#b76e00"
                                :else "#1e7e4e")
                  :flex-shrink 0}}]
   [:div {:style {:flex 1}}
    [:div {:style {:font-size "0.85rem" :color "#8FB8D4"}}
     date]
    [:div {:style {:font-size "0.9rem"}}
     event]
    [:div {:style {:font-size "0.8rem" :color "#aaa"}}
     (str "Fatigue: " (int (* 100 fatigue)) "%"
          (when-not available " — Unavailable"))]]])

(defn player-view []
  (let [history @(rf/subscribe [:player-history])]
    [:div
     [:h2 {:style {:margin-bottom "1rem" :color "#8FB8D4"}}
      "Player Timeline"]
     [:p {:style {:color "#aaa" :margin-bottom "1.5rem"
                  :font-size "0.9rem"}}
      "Development trajectory powered by temporal-squad.
       Each point represents the player's state at that
       valid-time — what the system knew when the record
       was created."]
     [:div {:class "card"}
      [:h3 {:style {:color "#8FB8D4" :margin-bottom "1rem"}}
       "Amara Diallo — Bi-Temporal Timeline (from XTDB)"]
      (if (empty? history)
        [:p {:style {:color "#aaa"}} "No history found for Amara Diallo. Make sure backend is running and seeded."]
        (for [point history]
          (let [doc        (:xtdb.api/doc point)
                valid-time (:xtdb.api/valid-time point)
                date-str   (if (string? valid-time)
                             (.substring valid-time 0 10)
                             (str valid-time))
                fatigue    (get doc :player/fatigue-idx 0.0)
                available  (get doc :player/available? true)
                event      (str "Status Update: " (:player/name doc)
                                (if available " is available" " is injured/fatigued"))]
            ^{:key date-str}
            [timeline-point {:date date-str
                             :fatigue fatigue
                             :available available
                             :event event}])))]]))
