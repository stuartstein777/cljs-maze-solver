(ns exfn.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :maze
 (fn [db _]
   (db :maze)))

(rf/reg-sub
 :path
 (fn [db _]
   (db :maze)))