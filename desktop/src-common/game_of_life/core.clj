(ns game-of-life.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [clojure.math.combinatorics :as combo]))

(declare game-of-life main-screen)

(def pixel-size 10)

(def co-ordinates (combo/cartesian-product (range 0 201 pixel-size) (range 0 201 pixel-size)))

(def initial-state 
  (->> co-ordinates 
       (map (fn [[x y]]
         (let [alive? (even? (rand-int 100))
               x-of-region (if alive? 0 pixel-size)]
           (assoc (texture "combined.jpeg" :set-region x-of-region 0 pixel-size pixel-size)
                  :x x :y y :width pixel-size :height pixel-size 
                  :isCell? true :alive? alive?))))))

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

(defn- get-next-state [entities {:keys [alive?] :as entity}] 
  (let [neighbor-states (find-neighbor-state entities entity)
        alive-neighbors (get neighbor-states true 0)
        dead-neighbors  (get neighbor-states false 0)]
        (cond
          (and alive?
               (or (< alive-neighbors 2) (> alive-neighbors 3))) false
          (and alive?
               (or (== alive-neighbors 2) (== alive-neighbors 3))) true
          (and (not alive?) (== alive-neighbors 3)) true
          :else alive?)))

(defn change-grid
  [entities]
  (loop [the-co-ordinates co-ordinates the-entities entities]
    (if (empty? the-co-ordinates)
      the-entities
     (let [current-x-y (first the-co-ordinates) 
           current-x (first current-x-y)
           current-y (second current-x-y)
           current-entity (first (filter #(and (= (:x %) current-x) (= (:y %) current-y)) the-entities))
           the-index (.indexOf the-entities current-entity)
           should-live (get-next-state the-entities current-entity)]
       (recur (next the-co-ordinates) 
              (assoc the-entities the-index (change-entity-state current-entity should-live)))))))
       
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

;(app! :post-runnable #(set-screen! game-of-life main-screen))
