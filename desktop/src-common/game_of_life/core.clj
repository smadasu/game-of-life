(ns game-of-life.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [clojure.math.combinatorics :as combo]))
(declare game-of-life main-screen)

(def pixel-size 10)

(def co-ordinates (combo/cartesian-product (range 0 201 pixel-size) (range 0 201 pixel-size)))

(def initial-state 
  (map (fn [[x y]]
         (let [alive? (even? (rand-int 100))
               x-of-region (if alive? 0 pixel-size)]
           (assoc (texture "combined.jpeg" :set-region x-of-region 0 pixel-size pixel-size)
                  :x x :y y :width pixel-size :height pixel-size 
                  :isCell? true :alive? alive?))) co-ordinates))

(def neighbors
  (into {} (map (fn [[x y]]
                  {(list x y)
                   (remove #(= % (list x y))
                           (combo/cartesian-product
                             (list (- x pixel-size) x (+ x pixel-size))
                             (list (- y pixel-size) y (+ y pixel-size))))}) co-ordinates)))

(defn- change-entity-state [entity live-state]
    (texture! entity
              :set-region (if live-state 0 pixel-size) 0 pixel-size pixel-size)
    (assoc entity :alive? live-state))

(defn- convert-to-cell [[x y] live-state]
  (assoc (texture
           (if live-state "alive.jpeg"
             "dead.jpeg")) :x x :y y :width pixel-size :height pixel-size :isCell? true :alive? live-state))

(defn find-neighbor-state [entities {:keys [x y] :as entity}]
    (frequencies 
      (map (fn [[neighbor-x neighbor-y]]
             (cond
               (or (< neighbor-x 0) (< neighbor-y 0)
                   (> neighbor-x 200) (> neighbor-y 200)) false
               :else 
               (if (some 
                     #(and (:isCell? %) (:alive? %) (== (:x %) neighbor-x) (== (:y %) neighbor-y))
                     entities) true false))) (get neighbors (list x y)))))

(defn apply-rules [entities {:keys [x y alive? isCell?] :as entity}]
  (if isCell? 
    (let [neighbor-states (find-neighbor-state entities entity)
          live-count (get neighbor-states true)
          alive-neighbors (if live-count live-count 0)
          dead-count (get neighbor-states false)
          dead-neighbors (if dead-count dead-count 0)
          should-live
          (cond
            (and (= true alive?) 
                 (or (< alive-neighbors 2) (> alive-neighbors 3))) false
            (and (= true alive?)
                 (or (== alive-neighbors 2) (== alive-neighbors 3))) true
            (and (= false alive?) (== alive-neighbors 3)) true
            :else alive?)]
      (change-entity-state entity should-live))
    entity))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage))
    (add-timer! screen :spawn-forms 0.3 0.3)
    (let [background (assoc (texture "background.jpeg") :width 200 :height 200)]
      (vec (conj initial-state background)))
    )

  :on-render
  (fn [screen entities]
    (clear!)
    (render! screen entities))

  :on-timer
  (fn [screen entities]
    (case (:id screen)
      ;:spawn-forms (reduce apply-rules entities entities)))
      :spawn-forms (map 
                     #(apply-rules entities %) entities)))

  :on-key-down
  (fn [screen entities]
    (cond
      (= (:key screen) (key-code :r))
      (app! :post-runnable #(set-screen! game-of-life main-screen))))

  )

(defgame game-of-life
  :on-create
  (fn [this]
    (set-screen! this main-screen)))

;(app! :post-runnable #(set-screen! game-of-life main-screen))
