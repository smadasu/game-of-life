(ns game-of-life.core.desktop-launcher
  (:require [game-of-life.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. game-of-life "game-of-life" 200 200)
  (Keyboard/enableRepeatEvents true))
