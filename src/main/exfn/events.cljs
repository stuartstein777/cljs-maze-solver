(ns exfn.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:maze (into [] (for [_ (range 0 (* 20 20))]
                     {:state :none}))
    :path []}))

(rf/reg-event-db
 :toggle-cell
 (fn [db [_ i]]
   (-> db
       (update-in [:maze i :state] #({:none :wall :wall :none} %)))))