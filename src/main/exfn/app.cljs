(ns exfn.app
  (:require [reagent.dom :as dom]
            [re-frame.core :as rf]
            [exfn.events]
            [exfn.subscriptions]))

;; -- Helpers -------------------------------------------------------

;; -- Reagent Forms --------------------------------------------------
(defn maze-tile [i {:keys [state] :as tile}]
  (let [attr {:key i
              :on-click #(rf/dispatch [:toggle-cell i])}]
    ({:wall [:div.map-tile.wall attr]
      :none [:div.map-tile.open attr]
      :start [:div.map-tile.start [:i.fas.fa-walking attr]]
      :finish [:div.map-tile.finish [:i.fas.fa-flag-checkered attr]]} state)))

;; map the maze-title map over the maze
;; partition and split.
;; on-click doesnt need x y coord, it just needs index.
;; how to get index to here. map indexed
(defn maze []
  (let [mz @(rf/subscribe [:maze])
        tiles (->> mz
                   (map-indexed maze-tile)
                   (partition 20)
                   (map (fn [row] [:div.flex-container row])))]
    [:div tiles]))

;; -- App ------------------------------------------------------------
(defn app []
  [:div.container
   [:h1 "Maze solver"]
   [maze]])

;; -- Dev Events -----------------------------------------------------
(rf/reg-event-db
 :set-finish
 (fn [db [_ i]]
   (assoc-in db [:maze i :state] :finish)))

(rf/reg-event-db
 :set-start
 (fn [db [_ i]]
   (assoc-in db [:maze i :state] :start)))

(comment (rf/dispatch [:set-start 130]))
;; -- After-Load -----------------------------------------------------
;; Do this after the page has loaded.
;; Initialize the initial db state.
(defn ^:dev/after-load start
  []
  (dom/render [app]
              (.getElementById js/document "app")))

(defn ^:export init []
  (start))

(defonce initialize (rf/dispatch-sync [:initialize]))