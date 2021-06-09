(ns exfn.solver
  (:require [clojure.set :as set]))

(defn get-dimensions [map]
  [(count (first map)) (count map)])

(defn find-in-map [m to-find [max-x max-y]]
  (prn to-find)
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
    (prn map-info)
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


(comment '[[:none :none :none :none :wall :wall :none :wall :none :wall :none :none :none :wall :none :none :none :none :wall :none]
           [:none :wall :wall :none :wall :none :start :wall :wall :wall :none :wall :none :wall :none :wall :wall :wall :wall :wall]
           [:none :none :wall :none :wall :none :none :wall :none :none :none :wall :none :wall :none :none :none :wall :none :wall]
           [:none :wall :wall :none :wall :none :wall :wall :none :wall :wall :wall :none :wall :wall :wall :none :wall :none :wall]
           [:wall :wall :none :none :wall :none :none :none :none :wall :none :none :none :none :wall :none :none :none :none :none]
           [:none :wall :none :wall :wall :none :wall :wall :wall :wall :wall :wall :wall :none :wall :wall :none :wall :wall :none]
           [:none :none :none :none :none :none :none :none :none :none :none :wall :finish :none :none :wall :none :wall :none :none]
           [:wall :none :wall :wall :none :none :wall :wall :wall :none :none :wall :none :wall :wall :wall :none :wall :wall :none]
           [:wall :wall :wall :none :none :wall :wall :none :wall :wall :wall :wall :none :wall :none :none :none :wall :none :none]
           [:wall :none :none :none :wall :wall :none :none :none :none :none :wall :none :wall :none :wall :none :wall :none :none]
           [:none :none :wall :wall :wall :none :none :wall :none :none :wall :wall :wall :wall :none :wall :none :wall :wall :none]
           [:none :wall :wall :none :none :none :none :wall :wall :wall :wall :none :none :none :none :wall :none :none :wall :wall]
           [:none :wall :none :none :wall :none :wall :wall :none :none :wall :none :none :wall :none :wall :none :none :none :none]
           [:none :wall :wall :none :wall :none :none :wall :none :none :none :none :wall :wall :wall :wall :wall :wall :none :wall]
           [:none :none :wall :none :wall :none :none :wall :none :wall :wall :wall :wall :none :none :wall :none :none :none :wall]
           [:wall :none :wall :none :wall :none :none :wall :none :wall :none :none :none :none :none :wall :none :wall :none :wall]
           [:wall :none :wall :none :wall :none :none :wall :wall :wall :none :wall :none :none :none :none :none :none :none :wall]
           [:wall :none :wall :wall :wall :none :none :none :none :none :none :wall :wall :wall :wall :wall :none :wall :wall :wall]
           [:wall :none :wall :none :wall :wall :wall :wall :wall :wall :wall :wall :none :wall :none :wall :none :wall :none :wall]
           [:wall :none :none :none :none :none :none :none :none :none :none :none :none :none :none :none :none :wall :none :none]])