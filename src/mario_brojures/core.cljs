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

(def abs js/Math.abs)

(defn check-collision [c1 c2]
  (let [vx (- (:x c1) (:x c2))
        vy (- (:y c1) (:y c2))
        hwidths 20
        hheights 20]
    (when (and (< (abs vx) hwidths)
               (< (abs vy) hheights))
      (let [ox (- hwidths (abs vx))
            oy (- hheights (abs vy))]
        (if (>= ox oy)
          (if (> vy 0)
            [:north oy]
            [:south oy])
          (if (> vx 0)
            [:west ox]
            [:east ox]))))))

(defn adjust-collid [collid dir delta]
  (let [[key delta'] (case dir
                       :north [:y delta]
                       :south [:y (- delta)]
                       :west [:x delta]
                       :east [:x (- delta)])]
    (assoc collid key delta')))

(defn process-collision [dir c1 c2])

(defn check-collisions [c1 collids]
  (reduce (fn [acc c2]
            (let [[o1 o2] (when (not= (:id c1) (:id c2))
                            (when-let [[dir delta] (check-collision c1 c2)]
                              (let [c1' (adjust-collid c1 dir delta)]
                                (process-collision c1' c2))))
                  conj-unless-nil #(if %2 (conj %1 %2) %1)]
              (-> acc
                  (cons-unless-nil o1)
                  (cons-unless-nil o2))))
          []
          collids))

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
        state {:player {:id 0
                        :x (/ cwidth 2) :y (/ cheight 2)
                        :status :standing
                        :dir :right
                        :sprite (sprite/make-small-player :standing :right)}
               :objs (for [[i type] (->> (keys sprite/enemy-sprite-params)
                                         (map-indexed list))]
                       {:id (inc i)
                        :x 50 :y (+ 50 (* 30 i)) :type type :dir :right
                        :sprite (sprite/make-enemy type :right)})}]
    (letfn [(update-helper [time state]
              (clear-canvas canvas)
              (let [dirs (translate-keys @pressed-keys)
                    player (update-player (:player state) dirs)
                    offset-x (mod (js/Math.floor (:x player)) bgd-width)]
                (draw-background context bgd offset-x)
                (doseq [obj (:objs state)]
                  (when (check-collision player obj)
                    (println "Collided with " (:type obj)))
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
