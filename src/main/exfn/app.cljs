(ns exfn.app
  (:require [reagent.dom :as dom]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [clojure.set :as set]))

;; -- Helpers -------------------------------------------------------

;;-- Events and Effects ---------------------------------------------
(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:maze (for [x (range 0 5)]
            (for [y (range 0 5)]
              {:coord [x y] :state :none}))
    :path []}))

(rf/reg-event-db
 :toggle-cell
 (fn [{:keys [maze] :as db} [_ [x y]]]
   (js/console.log (str "(" x "," y ")"))
    (let [toggle {:wall :none :none :wall}]
      (update-in db maze [y x] #(update % :state toggle)))))

;; -- Subscriptions --------------------------------------------------
(rf/reg-sub
 :maze
 (fn [db _]
   (db :maze)))

(rf/reg-sub
 :path
 (fn [db _]
   (db :maze)))

;; -- Reagent Forms --------------------------------------------------
(defn map-tile-background-color [state]
  (condp = state
    :wall :gray
    :start :blue
    :end :green
    :route :goldenyellow
    :none :white))

(defn maze-tile [{:keys [coord state] :as tile}]
  [:div.map-tile {:key coord
                  :style {:background-color (map-tile-background-color state)}
                  :on-click #(rf/dispatch [:toggle-cell coord])}])

(defn maze-row [mz row]
  [:div.flex-container
   (let [row (nth mz row)]
     (for [r row]
       [maze-tile r]))])

(defn maze []
  (let [mz @(rf/subscribe [:maze])]
    [:div.maze
     (map (partial maze-row mz) (range 0 5))]))

;; -- App ------------------------------------------------------------
(defn app []
  [:div.container
   [:h1 "Maze solver"]
   [maze]])

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