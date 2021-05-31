(ns main.exfn.scratch)

(let [db {:maze (into [] (for [x (range 0 4)]
                      (into [] (for [y (range 0 4)]
                                 {:state :none}))))}
      [row col] [3 2]
      toggle {:wall :none :none :wall}]
  (update db (db :maze) [row col] #(prn :foo))
  )
