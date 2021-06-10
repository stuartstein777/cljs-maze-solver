(ns exfn.solver
  (:require [clojure.set :as set]))

(defn path-val-to-xy [xy]
  [(quot xy 20) (rem xy 20)])

(defn get-route-markers [route]
  (map (fn [idx yx]
         (let [[y x] (path-val-to-xy yx)
               [prev-y prev-x] (path-val-to-xy (nth route (dec idx)))
               [next-y next-x] (path-val-to-xy (nth route (inc idx)))]
           (cond (= x next-x prev-x) [yx :vertical]
                 (= y next-y prev-y) [yx :horizontal]
                 (and (> prev-y y) (> next-x x) (= y next-y)) [yx :left-down]
                 (and (> prev-x x) (= prev-y y) (> next-y y)) [yx :left-down]
                 (and (< prev-y y) (> next-x x) (= y next-y)) [yx :down-right]
                 (and (= next-x x) (< next-y y) (> prev-x x)) [yx :down-right]
                 (and (= prev-y y) (> next-y y) (< prev-x x)) [yx :right-down]
                 (and (> x next-x) (> prev-y y) (= x prev-x)) [yx :right-down]
                 (and (= prev-y y) (< next-y y) (< prev-x x)) [yx :down-left]
                 (and (> x next-x) (= x prev-x) (< prev-y y)) [yx :down-left])))
       (range 1 (count route))
       (butlast (rest route))))

(defn get-dimensions [map]
  [(count (first map)) (count map)])

(defn find-in-map [m to-find [max-x max-y]]
  (first (for [y (range max-y)
               x (range max-x)
               :when (= to-find (get-in m [y x]))]
           [y x])))

(defn get-walls [m [max-x max-y]]
  (set (for [x (range max-x)
             y (range max-y)
             :when (= :wall (get-in m [y x]))]
         [y x])))

(defn get-map-info [m]
  (let [dimensions (get-dimensions m)]
    {:dimensions dimensions
     :start      (find-in-map m :start dimensions)
     :goal       (find-in-map m :finish dimensions)
     :walls      (get-walls m dimensions)}))

(defn coord-in-grid? [max-x max-y [y x]]
  (and (<= 0 x (dec max-x)) (<= 0 y (dec max-y))))

(defn get-neighbours [[max-x max-y] xy]
  (->> (map #(vec (map + (:cell xy) %)) [[-1 0] [1 0] [0 -1] [0 1]])
       (filter (partial coord-in-grid? max-x max-y))))

(defn f-heuristic [[goal-x goal-y] [cur-x cur-y] current-g]
  [(inc current-g) (+ (Math/abs (- cur-x goal-x)) (Math/abs (- cur-y goal-y)))])

(defn build-node-summary [goal parent node]
  (let [[g h] (f-heuristic goal node (:g parent))]
    {node {:cell node :parent (:cell parent) :g g :h h :f (+ g h)}}))

(defn get-next-cell-with-lowest-f-cost [open]
  (->> (map val open)
       (sort-by (juxt :f :h))
       (first)))

(defn find-route [start closed route]
  (let [parent-of-next (:parent (closed (last route)))]
    (if (= start parent-of-next)
      (reverse (conj route parent-of-next))
      (recur start closed (conj route parent-of-next)))))

(defn shorter-path? [new existing]
  (or (< (:f new) (:f existing))
      (and (= (:f new) (:f existing))
           (< (:g new) (:g existing)))))

(defn merge-with-open [open new]
  (reduce (fn [acc i]
            (let [new-cell (ffirst i)
                  new-values (second (first i))
                  existing (open new-cell)]
              (if (some? existing)
                (if (shorter-path? new-values existing)
                  (assoc acc (existing :cell) new-values)
                  acc)
                (merge acc i)))) open new))

(defn find-path [m]
  (let [map-info (get-map-info m)]
    (loop [closed {}
           open (assoc {} (:start map-info) {:cell   (:start map-info)
                                             :parent nil
                                             :g      0
                                             :h      (apply + (:goal map-info))
                                             :f      (apply + (:goal map-info))})]
      (cond (empty? open)
            (println "no path exists!")
            :else
            (let [current (get-next-cell-with-lowest-f-cost open)
                  updated-open (as-> (get-neighbours (:dimensions map-info) current) o
                                 (set o)
                                 (set/difference o (set/union (:walls map-info) (set (keys closed))))
                                 (map (partial build-node-summary (map-info :goal) current) o)
                                 (merge-with-open open o)
                                 (dissoc o (:cell current)))
                  updated-closed (assoc closed (:cell current) current)]
              (if (= (:cell current) (map-info :goal))
                (find-route (map-info :start) updated-closed [(map-info :goal)])
                (recur updated-closed updated-open)))))))

(defn solve [maze]
  (let [prepared-maze (->> maze
                           (map :state)
                           (partition 20)
                           (map vec)
                           vec)
        path (->> (find-path prepared-maze)
                  (map (fn [[x y]] (+ (* 20 x) y)))
                  (get-route-markers))]
    (zipmap (map first path) (map second path))))


(def test-map [:start :none :none :none :wall :wall :none :wall :none :wall :none :none :none :wall :none :none :none :finish :wall :none
               :none :wall :wall :none :wall :none :none :wall :wall :wall :none :wall :none :wall :none :wall :wall :wall :wall :wall
               :none :none :wall :none :wall :none :none :wall :none :none :none :wall :none :wall :none :none :none :wall :none :wall
               :none :wall :wall :none :wall :none :wall :wall :none :wall :wall :wall :none :wall :wall :wall :none :wall :none :wall
               :wall :wall :none :none :wall :none :none :none :none :none :none :none :none :none :wall :none :none :none :none :none
               :none :wall :none :wall :wall :none :wall :wall :wall :wall :wall :wall :wall :none :wall :wall :none :wall :wall :none
               :none :none :none :none :none :none :none :none :none :none :none :wall :none :none :none :wall :none :wall :none :none
               :wall :none :wall :wall :none :none :wall :wall :wall :none :none :wall :none :wall :wall :wall :none :wall :wall :none
               :wall :wall :wall :none :none :wall :wall :none :wall :wall :wall :wall :none :wall :none :none :none :wall :none :none
               :wall :none :none :none :wall :wall :none :none :none :none :none :wall :none :wall :none :wall :none :wall :none :none
               :none :none :wall :wall :wall :none :none :wall :none :none :wall :wall :wall :wall :none :wall :none :wall :wall :none
               :none :wall :wall :none :none :none :none :wall :wall :wall :wall :none :none :none :none :wall :none :none :wall :wall
               :none :wall :none :none :wall :none :wall :wall :none :none :wall :none :none :wall :none :wall :none :none :none :none
               :none :wall :wall :none :wall :none :none :wall :none :none :none :none :wall :wall :wall :wall :wall :wall :none :wall
               :none :none :wall :none :wall :none :none :wall :none :wall :wall :wall :wall :none :none :wall :none :none :none :wall
               :wall :none :wall :none :wall :none :none :wall :none :wall :none :none :none :none :none :wall :none :wall :none :wall
               :wall :none :wall :none :wall :none :none :wall :wall :wall :none :wall :none :none :none :none :none :none :none :wall
               :wall :none :wall :wall :wall :none :none :none :none :none :none :wall :wall :wall :wall :wall :none :wall :wall :wall
               :wall :none :wall :none :wall :wall :wall :wall :wall :wall :wall :wall :none :wall :none :wall :none :wall :none :wall
               :wall :none :none :none :none :none :none :none :none :none :none :none :none :none :none :none :none :wall :none :none])

(comment
  (let [path (->> test-map
                  (map (fn [cell] {:state cell}))
                  (vec)
                  (solve)
                  (get-route-markers))]
    (zipmap (map first path) (map second path))))





