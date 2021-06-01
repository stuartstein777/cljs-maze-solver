(ns main.exfn.scratch)

(let [tiles [{:state :none} {:state :none} {:state :none} {:state :none}
             {:state :none} {:state :none} {:state :none} {:state :none}
             {:state :none} {:state :none} {:state :none} {:state :none}
             {:state :none} {:state :none} {:state :none} {:state :none}]]
  (map-indexed (fn [i t] [i t]) tiles)
  )

