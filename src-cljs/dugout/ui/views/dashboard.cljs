(ns dugout.ui.views.dashboard
  (:require [re-frame.core :as rf]))

(defn stat-card [title value subtitle color]
  [:div {:class "card"
         :style {:border-left (str "4px solid " color)}}
   [:div {:style {:font-size "2rem" :font-weight "bold"
                  :color color}} value]
   [:div {:style {:font-weight "bold"
                  :margin-bottom "0.25rem"}} title]
   [:div {:style {:font-size "0.8rem" :color "#aaa"}}
    subtitle]])

(defn dashboard-view []
  (let [squad @(rf/subscribe [:squad])
        available (count (filter :available squad))
        high-risk (count (filter #(> (:fatigue % 0) 0.75)
                                 squad))]
    [:div
     [:h2 {:style {:margin-bottom "1.5rem"
                   :color "#8FB8D4"}}
      "dugout — Analytics Workbench"]
     [:p {:style {:color "#aaa" :margin-bottom "2rem"
                  :max-width 600}}
      "Open source football analytics infrastructure for
       African clubs. Five projects, one interface.
       No developer required to use it."]
     [:div {:style {:display "grid"
                    :grid-template-columns
                    "repeat(auto-fit, minmax(200px, 1fr))"
                    :gap "1rem"
                    :margin-bottom "2rem"}}
      [stat-card "Available Players"
       (str available "/" (count squad))
       "As of selection time" "#1e7e4e"]
      [stat-card "High Fatigue Risk"
       (str high-risk)
       "Above 75% fatigue threshold" "#b76e00"]
      [stat-card "Active Press Rules"
       "4"
       "coach-readable, modifiable" "#2E86C1"]
      [stat-card "Data Integrity"
       "✅"
       "Boundary-validated via pitch-pipe" "#1e7e4e"]]
     [:div {:class "card"}
      [:h3 {:style {:color "#8FB8D4"
                    :margin-bottom "1rem"}}
       "Infrastructure Stack"]
      [:p {:style {:font-size "0.9rem" :line-height 1.8}}
       "pitch-pipe — boundary validation + enrichment" [:br]
       "temporal-squad — bi-temporal player state (XTDB)"
       [:br]
       "press-logic — coach-readable tactical rules"
       [:br]
       "formation-stream — 25Hz tracking pipeline"
       [:br]
       "dugout — this interface"]]]))
