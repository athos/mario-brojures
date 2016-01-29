(ns mario-brojures.core
  (:refer-clojure :exclude [load]))

(enable-console-print!)

(def load-count (atom 0))
(def imgs-to-load 4)
(def level-width 2400)
(def level-height 256)

(def sprite-params
  {:small-player
   {:standing {:img-src "mario-small.png"
               :src-offset [0 0]
               :frame-size [16 16]
               :bbox-offset [3 1]
               :bbox-size [11 15]}}})

(defn make-from-params [params]
  (let [img (.createElement js/document "img")]
    (set! (.-src img) (str "sprites/" (:img-src params)))
    {:params params
     :img img
     }))

(defn render [context sprite dx dy]
  (let [{[sx sy] :src-offset, [sw sh] :frame-size} (:params sprite)]
    (.drawImage context (:img sprite) sx sy sw sh dx dy sw sh)))

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

(defn update-player-keys [player direction]
  (case direction
    :left (update player :x dec)
    :right (update player :x inc)
    :up (update player :y dec)
    :down (update player :y inc)))

(defn update-player [player directions]
  (let [player' (reduce update-player-keys player directions)]
    ;; some additional update goes here
    player'))

(defn update-loop [canvas]
  (let [scale 1
        context (.getContext canvas "2d")
        cwidth (/ (.-width canvas) scale)
        cheight (/ (.-height canvas) scale)
        sprite (-> (get-in sprite-params [:small-player :standing])
                   make-from-params)
        state {:player {:x (/ cwidth 2) :y (/ cheight 2)}}]
    (letfn [(update-helper [time state]
              (clear-canvas canvas)
              (let [dirs (translate-keys @pressed-keys)
                    player (update-player (:player state) dirs)]
                (render context sprite (:x player) (:y player))
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
