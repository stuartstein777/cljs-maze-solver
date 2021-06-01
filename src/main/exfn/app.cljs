(ns exfn.app
  (:require [reagent.dom :as dom]
            [re-frame.core :as rf]
            [exfn.events]
            [exfn.subscriptions]))

;; -- Helpers -------------------------------------------------------
(defn keyed-collection [col]
  (map vector (iterate inc 0) col))

;; -- Reagent Forms --------------------------------------------------
(defn maze-tile [i {:keys [state] :as tile}]
  (let [attr {:key i
              :on-click #(rf/dispatch [:toggle-cell i])}]
    ({:wall [:div.map-tile.wall attr]
      :none [:div.map-tile.open 
             attr
             [:div {:style {:border-bottom "1px dotted white"
                            :border-right "1px dotted white"
                            :width  "50%"
                            :height "50%"}}]
             [:div {:style {:border-bottom "1px dotted white"
                            :width  "50%"
                            :height "50%"}}]
             [:div {:style {:border-right "1px dotted white"
                            :width  "50%"
                            :height "50%"}}]
             [:div {:style {:width  "50%"
                            :height "50%"}}]]
      :start [:div.map-tile.start attr
              [:i.fas.fa-walking.walking-man]]
      :finish [:div.map-tile.finish attr
               [:i.fas.fa-flag-checkered.finish-flag]]} state)))

;; map the maze-title map over the maze
;; partition and split.
;; on-click doesnt need x y coord, it just needs index.
;; how to get index to here. map indexed
(defn maze []
  (let [mz @(rf/subscribe [:maze])
        tiles (->> mz
                   (map-indexed maze-tile)
                   (partition 20)
                   (map (fn [i row] [:div.flex-container {:key (str "r" i)} row]) (range 20)))]
    [:div {:style {:border "4px solid black"
                   :width 800}}tiles]))

(defn start-and-finish []
  (let [start-btn-title @(rf/subscribe [:start-btn-title])
        finish-btn-title @(rf/subscribe [:finish-btn-title])
        toggle-walls-btn-title @(rf/subscribe [:wall-btn-title])
        setting @(rf/subscribe [:setting])]
    [:div {:style {:margin-top 10}}
     [:button.btn.btn-primary.toggle-btn
      {:on-click #(rf/dispatch [:change-current-click :start])}
      start-btn-title]
     [:button.btn.btn-primary.toggle-btn
      {:on-click #(rf/dispatch [:change-current-click :finish])}
      finish-btn-title]
     [:button.btn.btn-primary.toggle-btn
      {:on-click #(rf/dispatch [:change-current-click :wall])}
      toggle-walls-btn-title]
     [:div.setting-indicator (str "Setting " (subs (str setting) 1))]]))

;; -- App ------------------------------------------------------------
(defn app []
  [:div.container
   [:h1 "Maze solver"]
   [maze]
   [start-and-finish]])

;; -- Dev Events -----------------------------------------------------
(rf/reg-event-db
 :set-wall
 (fn [db _]
   (assoc db :setting :wall)))

(comment (rf/dispatch [:set-wall]))
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