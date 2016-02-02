(ns mario-brojures.core
  (:refer-clojure :exclude [load])
  (:require [mario-brojures.sprite :as sprite]))

(enable-console-print!)

(def load-count (atom 0))
(def imgs-to-load 4)
(def level-width 2400)
(def level-height 256)

(defn render [context sprite dx dy]
  (let [{[sx sy] :src-offset, [sw sh] :frame-size} (:params sprite)]
    (.drawImage context (:img sprite) sx sy sw sh dx dy sw sh)))

(defn draw-background [context background offset-x]
  (let [background-size (get-in background [:params :frame-size])]
    (render context background (- offset-x) 0)
    (render context background (- (first background-size) offset-x) 0)))

(defn clear-canvas [canvas]
  (let [context (.getContext canvas "2d")
        cwidth (.-width canvas)
        cheight (.-height canvas)]
    (.clearRect context 0 0 cwidth cheight)))

(def pressed-keys
  (atom {:left false
         :right false
         :up false
         :down false}))

(defn translate-keys [pressed-keys]
  (->> pressed-keys
       (filter val)
       (map key)))

(defn update-player-keys [player control]
  (case control
    :left (-> player
              (update :x dec)
              (assoc :dir :left :status :running))
    :right (-> player
               (update :x inc)
               (assoc :dir :right :status :running))
    :up (-> player
            (update :y dec)
            (assoc :status :jumping))
    :down (-> player
              (update :y inc)
              (assoc :status :crouching))))

(defn update-player [player controls]
  (let [pl (reduce update-player-keys
                   (assoc player :status :standing)
                   controls)]
    (assoc pl :sprite (sprite/make-small-player (:status pl) (:dir pl)))))

(defn update-loop [canvas]
  (let [scale 1
        context (.getContext canvas "2d")
        cwidth (/ (.-width canvas) scale)
        cheight (/ (.-height canvas) scale)
        bgd (sprite/make-background)
        bgd-width (first (get-in bgd [:params :frame-size]))
        state {:player {:x (/ cwidth 2) :y (/ cheight 2)
                        :status :standing
                        :dir :right
                        :sprite (sprite/make-small-player :standing :right)}
               :objs (for [[i type] (->> (keys sprite/enemy-sprite-params)
                                         (map-indexed list))]
                       {:x 50 :y (+ 50 (* 30 i)) :type type :dir :right
                        :sprite (sprite/make-enemy type :right)})}]
    (letfn [(update-helper [time state]
              (clear-canvas canvas)
              (let [dirs (translate-keys @pressed-keys)
                    player (update-player (:player state) dirs)
                    offset-x (mod (js/Math.floor (:x player)) bgd-width)]
                (draw-background context bgd offset-x)
                (doseq [obj (:objs state)]
                  (render context (:sprite obj) (:x obj) (:y obj)))
                (render context (:sprite player) (:x player) (:y player))
                (.requestAnimationFrame js/window
                  (fn [t]
                    (update-helper t (assoc state :player player))))))]
      (update-helper 0 state))))

(defn keycode->key [code]
  (case code
    (38 32 87) :up
    (39 68) :right
    (37 65) :left
    (40 83) :down
    nil))

(defn keydown [e]
  (when-let [key (keycode->key (.-keyCode e))]
    (swap! pressed-keys assoc key true))
  true)

(defn keyup [e]
  (when-let [key (keycode->key (.-keyCode e))]
    (swap! pressed-keys assoc key false))
  true)

(defn load []
  (let [canvas (.getElementById js/document "canvas")]
    (.addEventListener js/document "keydown" keydown)
    (.addEventListener js/document "keyup" keyup)
    (update-loop canvas)))

(set! (.-onload js/window) load)
