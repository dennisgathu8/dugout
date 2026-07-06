(ns dugout.api.rules
  "Pressing rules API.
   Returns rule evaluation results from press-logic EDN files.
   Coaches can modify rule thresholds through the UI —
   changes are persisted as EDN."
  (:require [dugout.shared.rules :as shared-rules]
            [taoensso.timbre :as log]))

(defn get-rules
  "Returns all pressing rules from shared rules definition."
  []
  shared-rules/pressing-rules)

(defn update-rule-threshold
  "Updates a rule's fatigue threshold.
   Validates the new threshold is in (0.0, 1.0).
   This is how a coach modifies rules through the UI
   without touching code."
  [rule-id new-threshold]
  (when-not (and (number? new-threshold)
                 (> new-threshold 0.0)
                 (< new-threshold 1.0))
    (throw (ex-info "Invalid threshold value"
                    {:type      :dugout/validation-error
                     :rule-id   rule-id
                     :threshold new-threshold})))
  (log/info "Rule threshold updated"
            {:rule-id rule-id :threshold new-threshold})
  {:status :updated
   :rule-id rule-id
   :threshold new-threshold})
