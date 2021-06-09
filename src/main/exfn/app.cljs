(ns exfn.app
  (:require [reagent.dom :as dom]
            [re-frame.core :as rf]
            [exfn.events]
            [exfn.subscriptions]))

;; -- Helpers -------------------------------------------------------
(defn keyed-collection [col]
  (map vector (iterate inc 0) col))

;; -- Reagent Forms --------------------------------------------------
(defn maze-tile [path i {:keys [state] :as tile}]
  (let [attr {:key i
              :on-click #(rf/dispatch [:toggle-cell i])}]
    ({:wall [:div.map-tile.wall attr]
      :none [:div.map-tile.open
             {:key i
              :on-click #(rf/dispatch [:toggle-cell i])
              :style {:background-color (if (path i) "goldenrod" "white")}}
             [:div {:style {:border-bottom "0px dotted grey"
                            :border-right  "0px dotted grey"
                            :width         "50%"
                            :height        "50%"}}]
             [:div {:style {:border-bottom "0px dotted white"
                            :width         "50%"
                            :height        "50%"}}]
             [:div {:style {:border-right "0px dotted white"
                            :width        "50%"
                            :height       "50%"}}]
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
        path @(rf/subscribe [:path])
        tiles (->> mz
                   (map-indexed (partial maze-tile path))
                   (partition 20)
                   (map (fn [i row] [:div.flex-container {:key (str "r" i)} row]) (range 20)))]
    [:div {:style {:border "4px solid black"
                   :width 800}}
     tiles]))

(defn start-and-finish []
  (let [start-btn-title @(rf/subscribe [:start-btn-title])
        finish-btn-title @(rf/subscribe [:finish-btn-title])
        toggle-walls-btn-title @(rf/subscribe [:wall-btn-title])
        setting @(rf/subscribe [:setting])
        path @(rf/subscribe [:path])]
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
     [:button.btn.btn-primary
      {:on-click #(rf/dispatch [:solve])}
      "Solve"]
     [:label.no-solution
      {:style {:visibility (if (= #{} path) :visible :hidden)}}
      "No solution!"]
     [:div.setting-indicator (str "Setting " (subs (str setting) 1))]]))

;; -- App ------------------------------------------------------------
(defn app []
  [:div.container
   [:h1 "Maze solver"]
   [maze]
   [start-and-finish]])

;; -- Dev Events -----------------------------------------------------

(def test-maze 
  (vec (map (fn [m] {:state m}) [:none :none :none :none :wall :wall :none :wall :none :wall :none :none :none :wall :none :none :none :none :wall :none
                                 :none :wall :wall :none :wall :none :start :wall :wall :wall :none :wall :none :wall :none :wall :wall :wall :wall :wall
                                 :none :none :wall :none :wall :none :none :wall :none :none :none :wall :none :wall :none :none :none :wall :none :wall
                                 :none :wall :wall :none :wall :none :wall :wall :none :wall :wall :wall :none :wall :wall :wall :none :wall :none :wall
                                 :wall :wall :none :none :wall :none :none :none :none :wall :none :none :none :none :wall :none :none :none :none :none
                                 :none :wall :none :wall :wall :none :wall :wall :wall :wall :wall :wall :wall :none :wall :wall :none :wall :wall :none
                                 :none :none :none :none :none :none :none :none :none :none :none :wall :finish :none :none :wall :none :wall :none :none
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
                                 :wall :none :none :none :none :none :none :none :none :none :none :none :none :none :none :none :none :wall :none :none])))

(rf/reg-event-db
 :start-maze
 (fn [db _]
   (-> db
       (assoc :maze test-maze)
       (assoc :path #{})
       (assoc :start 26)
       (assoc :finish 132))))

(comment (rf/dispatch [:start-maze]))
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