(ns dugout.ui.views.squad
  "Squad Availability Dashboard.
   Shows who was available and what we knew at selection
   time — the temporal-squad bi-temporal capability
   made visual."
  (:require [re-frame.core :as rf]))

(defn fatigue-badge [fatigue]
  (let [level (cond (< fatigue 0.5) :fresh
                    (< fatigue 0.75) :moderate
                    :else :high-risk)
        label (case level
                :fresh    "Fresh"
                :moderate "Moderate"
                :high-risk "High Risk")
        cls   (case level
                :fresh    "badge-green"
                :moderate "badge-amber"
                :high-risk "badge-red")]
    [:span {:class (str "badge " cls)} label]))

(defn player-row [{:keys [name fatigue available]}]
  [:tr {:style {:border-bottom "1px solid #2E6DA4"}}
   [:td {:style {:padding "0.75rem"}} name]
   [:td {:style {:padding "0.75rem"}}
    [fatigue-badge fatigue]]
   [:td {:style {:padding "0.75rem"}}
    (str (int (* fatigue 100)) "%")]
   [:td {:style {:padding "0.75rem"}}
    (if available "✅ Available" "❌ Unavailable")]])

(defn squad-view []
  (let [squad @(rf/subscribe [:squad])
        high-risk @(rf/subscribe [:fatigue-high-risk])]
    [:div
     [:h2 {:style {:margin-bottom "1rem"
                   :color "#8FB8D4"}}
      "Squad Availability"]
     [:p {:style {:color "#aaa" :margin-bottom "1.5rem"
                  :font-size "0.9rem"}}
      "Showing availability as of selection time.
       Powered by temporal-squad bi-temporal queries —
       what the system knew when the decision was made,
       not what hindsight reveals."]
     (when (seq high-risk)
       [:div {:class "card"
              :style {:border-left "4px solid #8b1a1a"
                      :margin-bottom "1rem"}}
        [:strong {:style {:color "#ff6b6b"}}
         "⚠ High Fatigue Alert"]
        [:p {:style {:margin-top "0.5rem" :color "#aaa"}}
         (count high-risk)
         " player(s) above 75% fatigue threshold"]])
     [:div {:class "card"}
      [:table {:style {:width "100%"
                       :border-collapse "collapse"}}
       [:thead
        [:tr
         (for [h ["Player" "Fatigue Level" "Load %" "Status"]]
           [:th {:key h
                 :style {:text-align "left"
                         :padding "0.75rem"
                         :color "#8FB8D4"
                         :border-bottom "2px solid #2E6DA4"}}
            h])]]
       [:tbody
        (for [p squad]
          ^{:key (:player-id p)}
          [player-row p])]]]]))
