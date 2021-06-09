(ns main.exfn.scratch)

(let [maze [:none :none :none :none :none :none :none :none :none :none]
      path #{0 2 4 6 8}
      replacement :path]
  (map-indexed (fn [idx tile] (if (path idx) replacement tile)) maze))

(let [maze [:none :none :none :none :none :none :none :none :none :none]
      path #{0 2 4 6 8}]
  (reduce #(assoc %1 %2 :path) maze path))

(let [path [[14 4]
            [15 4]
            [16 4]
            [17 4]
            [18 4]
            [18 5]
            [18 6]
            [18 7]
            [18 8]]]
  (->> path
       (map (fn [[x y]] (+ (* 20 x) y)))
       (set)))