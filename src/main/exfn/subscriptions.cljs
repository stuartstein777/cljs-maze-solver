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

(rf/reg-sub
 :start-btn-title
 (fn [db _]
   (db :start-btn-title)))

(rf/reg-sub
 :finish-btn-title
 (fn [db _]
   (db :finish-btn-title)))

(rf/reg-sub
 :wall-btn-title
 (fn [db _]
   (db :wall-btn-title)))

(rf/reg-sub
 :setting
 (fn [db _]
   (db :setting)))

(rf/reg-sub
 :path
 (fn [db _]
   (db :path)))