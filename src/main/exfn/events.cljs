(ns exfn.events
  (:require [re-frame.core :as rf]
            [exfn.solver :as sve]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:finish           nil
    :finish-btn-title "Select Finish"
    :maze             (into [] (for [_ (range 0 (* 20 20))] {:state :none}))
    :path             []
    :setting          :wall
    :start            nil
    :start-btn-title  "Select Start"
    :wall-btn-title   "Toggle Walls"}))

(rf/reg-event-db
 :toggle-cell
 (fn [{:keys [finish setting start] :as db} [_ i]]
   (cond
     (= setting :wall)
     (update-in db [:maze i :state] #({:none :wall :wall :none :start :none :finish :none :path :wall} %))

     (= setting :start)
     (-> db
         (cond-> start
           (assoc-in [:maze start :state] :none))
         (assoc-in [:maze i :state] :start)
         (assoc :start i))

     (= setting :finish)
     (-> db
         (cond-> finish
           (assoc-in [:maze finish :state] :none))
         (assoc-in [:maze i :state] :finish)
         (assoc :finish i)))))

(rf/reg-event-db
 :change-current-click
 (fn [db [_ cur]]
   (assoc db :setting cur)))

(defn debug [o]
  (prn o)
  o)

(rf/reg-event-db
 :solve
 (fn [{:keys [maze] :as db} _]
   (let [prepared-maze (->> maze
                            (map :state)
                            (partition 20)
                            (map vec)
                            vec)
         path (->> (sve/find-path prepared-maze)
                   (drop 1)
                   (butlast))
         maze-route (->> path
                         (map (fn [[x y]] (+ (* 20 x) y)))
                         (set))]
     (-> db 
         (assoc :path path)
         (assoc :maze (vec (map-indexed (fn [idx tile] (if (maze-route idx) {:state :path} tile)) maze)))))))

