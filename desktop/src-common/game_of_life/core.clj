(ns game-of-life.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]))

(declare game-of-life main-screen)

(def pixel-size 10)

(def co-ordinates (for [row (range 0 201 pixel-size) 
                        column (range 0 201 pixel-size)]
                    (list row column)))

(def initial-state (map (fn [[x y]]
                          (assoc (texture "combined.jpeg" :set-region 
                                          (if (even? (rand-int 100)) 0 pixel-size) 0 pixel-size pixel-size)
                                 :x x :y y :width pixel-size :height pixel-size)
                          ) co-ordinates))

(def neighbors
  (into {} (map (fn [[x y]]
                  {(list x y)
                   (filter #(not= % (list x y))
                           (for [row (list (- x pixel-size) x (+ x pixel-size))
                                 column (list (- y pixel-size) y (+ y pixel-size))]
                             (list row column)))}) co-ordinates)))

(defn is-alive? [entity]
  (if (nil? entity) false
    (= 0 (texture! entity :get-region-x))))

(defn- change-entity-state [entity live-state]
  (texture! entity
            :set-region (if live-state 0 pixel-size) 0 pixel-size pixel-size))

(defn find-neighbor-state [entities {:keys [x y] :as entity}]
  (frequencies 
    (map (fn [[neighbor-x neighbor-y]]
           (is-alive?
             (first
               (filter #(and (== (:x %) neighbor-x) (== (:y %) neighbor-y)) entities))))
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
    (vec initial-state))

  :on-render
  (fn [screen entities]
    ;(clear!)
    ;(change-grid entities)
    (render! screen (change-grid entities)))

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
