(ns mario-brojures.core
  (:require ))

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
