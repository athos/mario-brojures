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

(defn update-loop [canvas]
  (let [context (.getContext canvas "2d")
        sprite (-> (get-in sprite-params [:small-player :standing])
                   make-from-params)]
    (letfn [(update-helper [time]
              (clear-canvas canvas)
              (render context sprite (/ time 100) (/ time 100))
              (.requestAnimationFrame js/window
                (fn [t]
                  (update-helper t))))]
      (update-helper 0))))

(defn load []
  (let [canvas (.getElementById js/document "canvas")]
    (update-loop canvas)))

(set! (.-onload js/window) load)
