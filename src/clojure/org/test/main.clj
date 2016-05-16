(ns org.test.main
    (:require [neko.activity :refer [defactivity set-content-view!]]
              [neko.debug :refer [*a]]
              [neko.notify :refer [toast]]
              [neko.resource :as res]
              [neko.find-view :refer [find-view]]
              [neko.threading :refer [on-ui]])
    (:import android.media.audiofx.Visualizer
             android.media.MediaPlayer
             android.media.AudioManager
             android.content.Context
             android.view.View
             android.view.animation.TranslateAnimation
             android.view.animation.RotateAnimation
             android.view.animation.Animation
             android.widget.EditText
             android.graphics.Canvas 
             android.graphics.Color
             android.graphics.Paint
             com.koushikdutta.ion.Ion
             android.net.Uri))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(defonce visualizer (atom nil))
(defonce data (atom nil))
(defonce beats (atom nil))

(defn make-beats!
  "Make the beats.  R$raw apprently doesn't exist but org.test.R$raw does"
  []
  (doto (MediaPlayer/create (*a) org.test.R$raw/oh)
    (.setLooping true)
    .start))

(defn make-visualizer!
  "Hooks into the beats and shoves the waveform into data at a certain rate."
  []
  (doto (Visualizer. (.getAudioSessionId @beats))
    (.setCaptureSize (nth (Visualizer/getCaptureSizeRange) 0))
    (.setDataCaptureListener
     (reify android.media.audiofx.Visualizer$OnDataCaptureListener 
       (onWaveFormDataCapture [this vis bytes sampling-rate]
         (swap! data (fn [_] bytes)) 
         (.invalidate (find-view (*a) ::viewFx)))
       (onFftDataCapture [this vis bytes sampling-rate]))
     (/ (Visualizer/getMaxCaptureRate) 2)
     true
     false)
    (.setEnabled true)))

(defn start!
  "Let the beats flow free"
  []
  (or @beats
      (swap! beats (fn [b] (or b
                               (make-beats!)))))
  (or @visualizer
      (swap! visualizer (fn [v] (or v
                                    (make-visualizer!))))))

(defn stop!
  "Pointless function.  Why stop?"
  []
  (when @beats
    (.stop @beats)
    (.release @beats))
  (when @visualizer
    (.setEnabled @visualizer false)
    (.release @visualizer))
  (reset! beats nil)
  (reset! visualizer nil))

(defn draw-visuals
  "Draw seizures and waveforms here.  The height should be a power of two and the type of data should be specified.  Do it unless desiring death by garbage collection."
  [^View view ^Canvas canvas ^Paint paint]
  (.drawColor canvas (Color/rgb (rand-int 255) (rand-int 255) (rand-int 255)))
  (when @data
    (let* [width  (.getWidth view)
           height (/ (.getHeight view) 2)
           height-multiplier (/ height 64)
           length (nth (Visualizer/getCaptureSizeRange) 0)]
      (dotimes [i (- length 1)]
        (.drawLine canvas
                   (* width (/ i (dec length)))
                   (+ height
                      (* (+ 128 (aget ^bytes @data i))
                         height-multiplier))                   
                   (* width (/ (inc i) (dec length)))
                   (+ height
                      (* (+ 128 (aget ^bytes @data (inc i)))
                         height-multiplier))
                   paint)))))

(defn make-viewfx 
  "Create the view that we draw the visual data on"
  [ctx]
  (let [paint (Paint.)]
    (doto paint
      (.setStrokeWidth 3.0)
      (.setAntiAlias true))
    (proxy [android.view.View] [ctx]
      (onDraw [^Canvas canvas]
        (draw-visuals this canvas paint)))))

(defn adjust-volume
  "Used to change the volume according to AudioManager/[ADJUST_LOWER or ADJUST_RAISE].  All it does now is blasting the volume"
  [activity]
  (let [audio (.getSystemService activity Context/AUDIO_SERVICE)]
    (.setStreamVolume audio
                     AudioManager/STREAM_MUSIC
                     (.getStreamMaxVolume audio AudioManager/STREAM_MUSIC)
                     0)))

(defn dance
  "Load up the gif and dance!"
  [activity] 
  (doto (find-view activity ::dance)
    (.setX 0)
    (.startAnimation (doto (TranslateAnimation. 0 422 0 0)
                       (.setDuration 5000)
                       (.setRepeatCount Animation/INFINITE)
                       (.setRepeatMode Animation/REVERSE))))
  (doto (find-view activity ::DO-IT)
    (.startAnimation (doto (RotateAnimation. 0 360 48 48)
                       (.setDuration 2000)
                       (.setRepeatCount Animation/INFINITE)
                       (.setRepeatMode Animation/REVERSE))))
  (.startAnimation (find-view activity ::back)
                   (doto (TranslateAnimation. 0 0 0 145)
                     (.setDuration 1000)
                     (.setRepeatCount Animation/INFINITE)
                     (.setRepeatMode Animation/REVERSE))))

(defn rotate
  "Rotate the the view by the angle."
  [view-id angle]
  (let [view (find-view (*a) view-id)]
    (.setRotation view (+ angle (.getRotation view)))))

(defn dance-button
  "When you touch that thing, this will happens"
  [activity]
  (toast "ORESAMA - Ookami Heart")
  (start!)
  (dance activity)
  (adjust-volume activity))

(defn load-dancer
  "Create the dancing gif"
  [activity]
  (.load (Ion/with (find-view activity ::dance))
         (str "android.resource://"
              (.getPackageName activity)
              "/"
              R$drawable/ppd)))

(defn make-layout []
  "Just the entire layout for the app.  A vector should be returned."
  [:linear-layout {:orientation :vertical
                   :layout-width :fill
                   :layout-height :wrap
                   :background-color (Color/parseColor "#ff1b74ff")}
   ;; Upper Half
   [:relative-layout {:layout-height 512
                      :on-click (fn [_] (toast "Don't Touch Me"))}
    [:relative-layout {:id ::back
                       :layout-width :fill
                       :layout-height :wrap
                       :background-color (Color/parseColor "#ffffaaff")}
     [:image-view {:id ::dance
                   :layout-center-horizontal true}]]]
   ;; Bottom Half
   [:relative-layout {:layout-height :wrap}
    [:view {:id ::viewFx
            :layout-align-parent-bottom true
            :layout-height 512
            :custom-constructor make-viewfx}]
    [:relative-layout {:layout-align-top ::viewFx
                       :layout-width :fill
                       :layout-height 256
                       :background-color (Color/parseColor "#d9f64c6d")}
     [:button {:text "<-"
               :layout-align-parent-left true
               :layout-center-vertical true
               :background-color (Color/parseColor "#ddfff74e")
               :on-click (fn [_] (rotate ::dance -30))}]
     [:image-view {:id ::DO-IT
                   :layout-center-horizontal true
                   :layout-center-vertical true 
                   :image R$drawable/clj
                   :on-click (fn [_] (dance-button (*a)))}]
     [:button {:text "->"
               :background-color (Color/parseColor "#ddfff74e")
               :layout-align-parent-right true
               :layout-center-vertical true
               :on-click (fn [_] (rotate ::dance 30))}]]]])

(defactivity org.test.MainActivity 
  :key :main
  (onCreate
   [this bundle]
   (.superOnCreate this bundle)
   (neko.debug/keep-screen-on this)
   (.requestPermissions this (into-array ["android.permission.RECORD_AUDIO"])
                        200)
   ;; Re-evaluate ON-UI to make layout changes
   (on-ui (set-content-view! (*a) (make-layout))
          (load-dancer (*a)))
   ))
