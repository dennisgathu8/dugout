(ns dugout.ui.views.pressing
  "Pressing Analysis Map.
   Visual 18-zone pitch showing where pressing triggers
   fired, failed, and where fatigue limited execution.
   Powered by press-logic rules."
  (:require [re-frame.core :as rf]
            [dugout.shared.zones :as zones]))

(defn zone-color [zone-key press-data]
  (let [events (get press-data zone-key {:fired 0 :missed 0
                                          :fatigue 0})]
    (cond
      (> (:fired events 0) 2)    "#1e7e4e"
      (> (:fatigue events 0) 1)  "#b76e00"
      (> (:missed events 0) 1)   "#8b1a1a"
      :else                       "#1A3A5C")))

(defn pitch-zone [zone-num x y w h press-data]
  (let [zone-key (keyword (str "zone"
                                (when (< zone-num 10) "0")
                                zone-num))
        label    (get zones/zone-labels zone-key "")]
    [:g {:key zone-key}
     [:rect {:x x :y y :width w :height h
             :fill (zone-color zone-key press-data)
             :stroke "#2E6DA4" :stroke-width 1
             :opacity 0.8}]
     [:text {:x (+ x (/ w 2)) :y (+ y (/ h 2))
             :text-anchor "middle"
             :dominant-baseline "middle"
             :fill "#fff" :font-size "9"
             :font-family "Arial"}
      label]]))

(defn pressing-map [press-data]
  (let [zone-w (/ 540 3)
        zone-h (/ 360 6)]
    [:svg {:viewBox "0 0 540 360"
           :class "pitch"
           :style {:background "#1a4a1a"}}
     ;; Draw all 18 zones: 3 thirds x 6 channels
     (for [third (range 3)
           channel (range 6)]
       (let [zone-num (inc (+ (* third 6) channel))
             x (* third zone-w)
             y (* channel zone-h)]
         [pitch-zone zone-num x y
          zone-w zone-h press-data]))
     ;; Goal
     [:rect {:x 510 :y 140 :width 30 :height 80
             :fill "none" :stroke "#fff"
             :stroke-width 2}]]))

(defn pressing-view []
  (let [rules @(rf/subscribe [:rules])
        ;; Synthetic press data for demo
        demo-data {:zone09 {:fired 3 :missed 1 :fatigue 0}
                   :zone10 {:fired 2 :missed 2 :fatigue 1}
                   :zone15 {:fired 4 :missed 0 :fatigue 0}
                   :zone16 {:fired 1 :missed 3 :fatigue 2}
                   :zone03 {:fired 0 :missed 2 :fatigue 1}}]
    [:div
     [:h2 {:style {:margin-bottom "1rem" :color "#8FB8D4"}}
      "Pressing Analysis"]
     [:div {:style {:display "flex" :gap "2rem"
                    :flex-wrap "wrap"}}
      [:div {:style {:flex 1}}
       [pressing-map demo-data]
       [:div {:style {:margin-top "1rem"
                      :display "flex" :gap "1rem"}}
        (for [[color label]
              [["#1e7e4e" "Press fired"]
               ["#b76e00" "Fatigue limited"]
               ["#8b1a1a" "Missed — conditions met"]
               ["#1A3A5C" "No trigger"]]]
          [:div {:key label
                 :style {:display "flex"
                         :align-items "center"
                         :gap "0.5rem"}}
           [:div {:style {:width 16 :height 16
                          :background color
                          :border-radius 3}}]
           [:span {:style {:font-size "0.8rem"
                           :color "#aaa"}}
            label]])]]
      [:div {:style {:width 280}}
       [:h3 {:style {:color "#8FB8D4"
                     :margin-bottom "1rem"}}
        "Active Rules"]
       (for [rule rules]
         [:div {:key (str (:rule/id rule))
                :class "card"
                :style {:margin-bottom "0.75rem"}}
          [:strong {:style {:font-size "0.9rem"}}
           (:rule/name rule)]
          [:p {:style {:font-size "0.8rem"
                       :color "#aaa"
                       :margin-top "0.25rem"}}
           "Fatigue threshold: "
           [:strong
            (int (* 100 (:rule/fatigue-threshold rule)))
            "%"]]
          [:input {:type "range"
                   :min 0 :max 100
                   :value (int (* 100
                                  (:rule/fatigue-threshold
                                   rule 0.7)))
                   :on-change
                   (fn [e]
                     (rf/dispatch
                      [:update-rule-threshold
                       (:rule/id rule)
                       (/ (.. e -target -value) 100)]))}]])]]]))
