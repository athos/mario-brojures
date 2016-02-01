(ns mario-brojures.sprite)

(defn setup-sprite [img-src frame-size src-offset
                    & {:keys [bbox-offset bbox-size]
                       :or {bbox-offset [0 0], bbox-size [0 0]}}]
  (with-meta
    {:img-src img-src
     :frame-size frame-size
     :src-offset src-offset
     :bbox-offset bbox-offset
     :bbox-size bbox-size}
    {:tag :sprite-params}))

(defn sprite-params? [x]
  (= (:tag (meta x)) :sprite-params))

(def player-sprite-params
  {:small-player
   {:left
    {:standing  (setup-sprite "mario-small.png" [16 16] [0 0]
                              :bbox-offset [3 1] :bbox-size [11 15])
     :jumping   (setup-sprite "mario-small.png" [16 16] [16 16]
                              :bbox-offset [2 1] :bbox-size [13 15])
     :running   (setup-sprite "mario-small.png" [16 16] [16 0]
                              :bbox-offset [2 1] :bbox-size [12 15])
     :crouching (setup-sprite "mario-small.png" [16 16] [0 64]
                              :bbox-offset [1 5] :bbox-size [14 10])}
    :right
    {:standing  (setup-sprite "mario-small.png" [16 16] [0 32]
                              :bbox-offset [1 1] :bbox-size [11 15])
     :jumping   (setup-sprite "mario-small.png" [16 16] [16 48]
                              :bbox-offset [2 1] :bbox-size [13 15])
     :running   (setup-sprite "mario-small.png" [16 16] [16 32]
                              :bbox-offset [2 1] :bbox-size [12 15])
     :crouching (setup-sprite "mario-small.png" [16 16] [0 64]
                              :bbox-offset [1 5] :bbox-size [14 10])}}})

(def enemy-sprite-params
  {:goomba (setup-sprite "enemies.png" [16 16] [0 128]
                         :bbox-offset [1 1] :bbox-size [14 14])
   :gkoopa
   {:left  (setup-sprite "enemies.png" [16 27] [0 69]
                         :bbox-offset [4 10] :bbox-size [11 16])
    :right (setup-sprite "enemies.png" [16 27] [32 69]
                         :bbox-offset [1 10] :bbox-size [11 16])}
   :rkoopa
   {:left  (setup-sprite "enemies.png" [16 27] [0 5]
                         :bbox-offset [4 10] :bbox-size [11 16])
    :right (setup-sprite "enemies.png" [16 27] [32 5]
                         :bbox-offset [1 10] :bbox-size [11 16])}
   :gkoopashell (setup-sprite "enemies.png" [16 16] [0 96]
                              :bbox-offset [2 2] :bbox-size [12 13])
   :rkoopashell (setup-sprite "enemies.png" [16 16] [0 32]
                              :bbox-offset [2 2] :bbox-size [12 13])})

(defn make-from-params [params]
  (let [img (.createElement js/document "img")]
    (set! (.-src img) (str "sprites/" (:img-src params)))
    {:params params
     :img img
     }))

(defn make-small-player [status dir]
  (-> (get-in player-sprite-params [:small-player dir status])
      make-from-params))

(defn make-enemy [type & [dir]]
  (let [maybe-params (get enemy-sprite-params type)
        params (if (sprite-params? maybe-params)
                 maybe-params    ; dir can be ignored for this type of enemy
                 (get maybe-params dir))]
    (make-from-params params)))

(defn make-background []
  (-> (setup-sprite "bgd-1.png" [512 256] [0 0])
      make-from-params))
