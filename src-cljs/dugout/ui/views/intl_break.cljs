(ns dugout.ui.views.intl-break
  "International Break Impact view.
   Addresses the AFC Leopards pattern."
  (:require [re-frame.core :as rf]))

(defn impact-row [{:keys [name fatigue-before fatigue-after fatigue-delta]}]
  [:tr {:style {:border-bottom "1px solid #2E6DA4"}}
   [:td {:style {:padding "0.75rem"}} name]
   [:td {:style {:padding "0.75rem"}} (str (int (* 100 fatigue-before)) "%")]
   [:td {:style {:padding "0.75rem"}} (str (int (* 100 fatigue-after)) "%")]
   [:td {:style {:padding "0.75rem"
                 :color (if (> fatigue-delta 0.1) "#ff6b6b" "#4ec94e")
                 :font-weight "bold"}}
    (str (when (> fatigue-delta 0) "+") (int (* 100 fatigue-delta)) "%")]])

(defn intl-break-view []
  (let [impact @(rf/subscribe [:intl-break])]
    [:div
     [:h2 {:style {:margin-bottom "1rem" :color "#8FB8D4"}} "International Break Impact"]
     [:div {:class "card" :style {:border-left "4px solid #b76e00" :margin-bottom "1.5rem"}}
      [:strong "The AFC Leopards Pattern"]
      [:p {:style {:color "#aaa" :margin-top "0.5rem" :font-size "0.9rem"}}
       "AFC Leopards dropped points 3 times this season in matches immediately after international breaks. This view shows exactly what the fitness data said at selection time — not what hindsight reveals. Powered by temporal-squad bi-temporal queries."]]
     [:div {:class "card"}
      [:table {:style {:width "100%" :border-collapse "collapse"}}
       [:thead
        [:tr (for [h ["Player" "Fatigue Before Break" "Fatigue After Break" "Change"]]
               [:th {:key h :style {:text-align "left" :padding "0.75rem" :color "#8FB8D4" :border-bottom "2px solid #2E6DA4"}} h])]]
       [:tbody
        (if (empty? impact)
          [:tr
           [:td {:col-span 4 :style {:padding "1rem" :text-align "center" :color "#aaa"}}
            "No data loaded. Make sure the backend is seeded."]]
          (for [p impact]
            ^{:key (:player-id p)} [impact-row p]))]]]]))
