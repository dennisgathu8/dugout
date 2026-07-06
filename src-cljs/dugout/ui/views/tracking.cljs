(ns dugout.ui.views.tracking
  "Live Tracking Monitor view.
   Real-time compactness and pressing intensity metrics
   streamed via WebSocket from formation-stream."
  (:require [re-frame.core :as rf]))

(defn metric-gauge [label value max-val color]
  (let [pct (min 100 (* (/ value max-val) 100))]
    [:div {:style {:margin-bottom "1rem"}}
     [:div {:style {:display "flex" :justify-content "space-between" :margin-bottom "0.25rem"}}
      [:span {:style {:font-size "0.85rem" :color "#8FB8D4"}} label]
      [:span {:style {:font-size "0.85rem" :font-weight "bold" :color color}} (str (.toFixed value 1) "m")]]
     [:div {:style {:background "#0D2137" :border-radius 4 :height 8 :overflow "hidden"}}
      [:div {:style {:width (str pct "%") :height "100%" :background color :border-radius 4 :transition "width 0.3s ease"}}]]]))

(defn tracking-view []
  (let [live-data   @(rf/subscribe [:tracking-live])
        compactness (or (:compactness live-data) {:length 35.2 :width 42.8 :ratio 28.5})
        pressing    (or (:pressing live-data) {:nearest3 8.2 :ppda 12.4 :trigger 6.1})
        is-live?    (some? live-data)]
    [:div
     [:h2 {:style {:margin-bottom "1rem" :color "#8FB8D4"}} "Live Tracking Monitor"]
     [:p {:style {:color "#aaa" :margin-bottom "1.5rem" :font-size "0.9rem"}}
      "Real-time formation compactness and pressing intensity. In production, this streams from formation-stream's 25Hz tracking pipeline via WebSocket."]
     [:div {:style {:display "grid" :grid-template-columns "repeat(auto-fit, minmax(280px, 1fr))" :gap "1rem"}}
      [:div {:class "card"}
       [:h3 {:style {:color "#8FB8D4" :margin-bottom "1rem"}} "Team Compactness"]
       [metric-gauge "Length (front to back)" (:length compactness) 60.0 "#2E86C1"]
       [metric-gauge "Width (side to side)" (:width compactness) 68.0 "#1e7e4e"]
       [metric-gauge "Compactness ratio" (:ratio compactness) 50.0 "#b76e00"]]
      [:div {:class "card"}
       [:h3 {:style {:color "#8FB8D4" :margin-bottom "1rem"}} "Pressing Intensity"]
       [metric-gauge "Mean nearest 3 defenders" (:nearest3 pressing) 20.0 "#2E86C1"]
       [metric-gauge "PPDA (passes per def. action)" (:ppda pressing) 25.0 "#1e7e4e"]
       [metric-gauge "High press trigger distance" (:trigger pressing) 15.0 "#b76e00"]]
      [:div {:class "card"}
       [:h3 {:style {:color "#8FB8D4" :margin-bottom "1rem"}} "Connection Status"]
       [:div {:style {:display "flex" :align-items "center" :gap "0.5rem" :margin-bottom "1rem"}}
        [:div {:style {:width 10 :height 10 :border-radius "50%"
                       :background (if is-live? "#1e7e4e" "#b76e00")}}]
        [:span {:style {:font-size "0.9rem"}}
         (if is-live? "Connected — Live 25Hz Stream" "Demo Mode — Synthetic Data")]]
       [:p {:style {:font-size "0.8rem" :color "#aaa"}}
        "In production, connect to formation-stream WebSocket endpoint for live 25Hz tracking data from GPS/optical tracking systems."]]]]))
