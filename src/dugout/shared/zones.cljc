(ns dugout.shared.zones
  "Pitch zone definitions shared between JVM and browser.
   StatsBomb pitch: x 0-120, y 0-80.
   18 zones: 3 horizontal thirds x 6 vertical channels.
   These are the same zone keywords used in pitch-pipe,
   press-logic, and formation-stream.")

(defn- zero-pad
  "Zero-pads a number to 2 digits. Cross-platform."
  [n]
  (if (< n 10) (str "0" n) (str n)))

(defn compute-zone
  "Maps a StatsBomb [x y] location to a zone keyword.
   Returns :zone01 through :zone18.
   This function runs identically on the JVM and in
   the browser — the .cljc architectural proof."
  [[x y]]
  (let [h-third (cond (< x 40.0) 0
                      (< x 80.0) 1
                      :else 2)
        v-band  (min 5 (int (/ y (/ 80.0 6))))
        zone-num (+ (* h-third 6) v-band 1)]
    (keyword (str "zone" (zero-pad zone-num)))))

(def zone-labels
  "Human-readable zone labels for UI display."
  {:zone01 "Def Left"   :zone02 "Def Ctr-L" :zone03 "Def Ctr"
   :zone04 "Def Ctr-R"  :zone05 "Def Right" :zone06 "Def Wide"
   :zone07 "Mid Left"   :zone08 "Mid Ctr-L" :zone09 "Mid Ctr"
   :zone10 "Mid Ctr-R"  :zone11 "Mid Right" :zone12 "Mid Wide"
   :zone13 "Att Left"   :zone14 "Att Ctr-L" :zone15 "Att Ctr"
   :zone16 "Att Ctr-R"  :zone17 "Att Right" :zone18 "Att Wide"})

(def pressing-zones
  "Zones where pressing is tactically expected.
   Derived from press-logic rule definitions."
  {:high-press    #{:zone13 :zone14 :zone15
                    :zone16 :zone17 :zone18
                    :zone07 :zone08 :zone09
                    :zone10 :zone11 :zone12}
   :mid-block     #{:zone07 :zone08 :zone09
                    :zone10 :zone11 :zone12}
   :rest-defence  #{:zone01 :zone02 :zone03
                    :zone04 :zone05 :zone06}
   :counterpress  #{:zone07 :zone08 :zone09
                    :zone10 :zone11 :zone12
                    :zone13 :zone14 :zone15
                    :zone16 :zone17 :zone18}})
