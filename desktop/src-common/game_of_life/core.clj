(ns game-of-life.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [clojure.math.combinatorics :as combo]))

(declare game-of-life main-screen)

(def pixel-size 10)

(def co-ordinates (combo/cartesian-product (range 0 201 pixel-size) (range 0 201 pixel-size)))

(defn convert-x-y-to-cell [[x y]]
  (assoc (texture "combined.jpeg" :set-region 
                  (if (even? (rand-int 100)) 0 pixel-size) 0 pixel-size pixel-size)
         :x x :y y :width pixel-size :height pixel-size))

(def initial-state 
  (->> co-ordinates 
       (map convert-x-y-to-cell)))

(def neighbors
  (into {} (map (fn [[x y]]
                  {(list x y)
                   (filter #(not= % (list x y))
                           (combo/cartesian-product
                             (list (- x pixel-size) x (+ x pixel-size))
                             (list (- y pixel-size) y (+ y pixel-size))))}) co-ordinates)))

(defn is-alive? [entity]
  (= 0 (texture! entity :get-region-x)))

(defn- change-entity-state [entity live-state]
  (texture! entity
            :set-region (if live-state 0 pixel-size) 0 pixel-size pixel-size))

(defn find-neighbor-state [entities {:keys [x y] :as entity}]
  (frequencies 
    (map (fn [[neighbor-x neighbor-y]]
           (if
             (or (< neighbor-x 0) (< neighbor-y 0)
                 (> neighbor-x 200) (> neighbor-y 200)) false
             (is-alive? 
               (first 
                 (filter #(and (== (:x %) neighbor-x) (== (:y %) neighbor-y)) entities)))))
         (get neighbors (list x y)))))

(defn- get-next-state [entities entity] 
  (let [neighbor-states (find-neighbor-state entities entity)
        alive-neighbors (get neighbor-states true 0)
        dead-neighbors  (get neighbor-states false 0)
        alive? (is-alive? entity)]
    (cond
      (and alive?
           (or (< alive-neighbors 2) (> alive-neighbors 3))) false
      (and alive?
           (or (== alive-neighbors 2) (== alive-neighbors 3))) true
      (and (not alive?) (== alive-neighbors 3)) true
      :else alive?)))

(defn change-grid
  [entities]
  (loop [the-co-ordinates co-ordinates]
    (if (empty? the-co-ordinates)
      entities
      (let [current-x-y (first the-co-ordinates) 
            current-x (first current-x-y)
            current-y (second current-x-y)
            current-entity (first (filter #(and (= (:x %) current-x) (= (:y %) current-y)) entities))
            should-live (get-next-state entities current-entity)]
        (change-entity-state current-entity should-live)
        (recur (next the-co-ordinates))))))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage))
    (add-timer! screen :spawn-forms 0.2 0.2)
    (vec initial-state))

  :on-render
  (fn [screen entities]
    (clear!)
    (render! screen entities))

  :on-timer
  (fn [screen entities]
    (case (:id screen)
      :spawn-forms (change-grid entities)))

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
