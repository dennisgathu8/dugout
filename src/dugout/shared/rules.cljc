(ns dugout.shared.rules
  "Pressing rule data structures shared between backend
   evaluation (press-logic) and frontend display (rule editor).")

(def pressing-rules
  "The four pressing rules from press-logic, represented
   as data for both backend evaluation and frontend display.
   A coach can modify these through the rule editor UI —
   changes are sent to the backend as EDN and persisted."
  [{:rule/id          :press/high
    :rule/name        "High Press"
    :rule/description "Press when ball is in attacking or middle third, player is under pressure, and pressing player fatigue is below threshold."
    :rule/fatigue-threshold 0.70
    :rule/weight      0.85}
   {:rule/id          :press/mid-block
    :rule/name        "Mid Block Press"
    :rule/description "Press in the middle third to force backward passes and reset opponent build-up."
    :rule/fatigue-threshold 0.60
    :rule/weight      0.65}
   {:rule/id          :press/rest-defence
    :rule/name        "Rest Defence Press"
    :rule/description "Press in the defensive third — strict fatigue requirement. Pressing when fatigued here is high risk."
    :rule/fatigue-threshold 0.40
    :rule/weight      0.90}
   {:rule/id          :press/counter
    :rule/name        "Counterpressing"
    :rule/description "Press immediately after possession loss in the middle or attacking third. The 5-second window."
    :rule/fatigue-threshold 0.65
    :rule/weight      0.80}])
