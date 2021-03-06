(ns rwl.star-flight-3d
  "A 3d space flight game, where players take control of a ship which can freely
  go in any direction in 3d space within a boundary. Player locations are kept
  track of in a hashmap, mapping player number to [x,y,z] coordinates. Each
  player has their own thread, and so a readers-writer lock is put on the
  map. In this performance test, we compare two different readers-writer locks'
  performance. To focus our measurements only on lock performance, we disable
  collision detection, and so threads simply move their player, or take a
  snapshot of the map at a given time. The ships are outfitted with Finite
  Improbability Drives, which means that each time they move, they wind up
  no more than 100 units along any axis away from the origin (reduces overhead
  associated with incrementing the current position).  The percentage of the
  time that each event happens is fixed for a full set of tests, and then
  incremented."
  {:author "Dan Wysocki"}
  (:require [incanter core charts stats]
            [rwl.util :as util]
            [rwl.locks.countdown-semaphore-lock :refer [CSL-atomic]]
            [rwl.locks.reentrant-rwl :refer [RRWL-atomic]])
  (:import [java.util.concurrent CountDownLatch]))

(defn play-game
  [num-players read-portion read-fn write-fn]
  (let [player-action #(util/prob read-portion read-fn write-fn %)]
    (util/dopool player-action (range num-players) num-players)))

(defn play-game-throughput
  "Creates a pool of players which continually run until time-ns has elapsed.
  The counter argument must be an agent which you have external access to.
  The throughput will be whatever the final value of counter per time-ns."
  [num-players read-portion rwl-fn ^java.lang.Long time-ns]
  (let [rwl                    (rwl-fn {})
        counter                (agent 0N)
        count-down             (new CountDownLatch num-players)
        read-fn               #(rwl :read)
        write-fn               (fn [x] (rwl :write assoc x
                                          ; writes random [x,y,z] coordinate
                                            (take 3 (rand-int 100))))
        end-time               (future (+ (System/nanoTime) time-ns))
        [read-fn-t write-fn-t] (for [func [read-fn write-fn]]
                                 (fn [& args]
                                   ; The countdown and await only happens once
                                   ; for each player
                                   (.countDown count-down)
                                   (.await count-down)
                                   (loop [args args]
                                     (apply func args)
                                     (if (< (System/nanoTime)
                                            @end-time)
                                       (do (send counter inc)
                                           (recur args))))))]
       (play-game num-players read-portion read-fn-t write-fn-t)
       (await counter)
       (/ @counter (/ time-ns 1000000))))

(defmacro play-game-throughput-CSL
  "Tests throughput using a countdown-semaphore-lock"
  [num-players read-portion ^java.lang.Long time-ns]
  `(play-game-throughput num-players read-portion CSL-atomic time-ns))

(defmacro play-game-throughput-RRWL
  "Tests throughput using a countdown-semaphore-lock"
  [num-players read-portion ^java.lang.Long time-ns]
  `(play-game-throughput num-players read-portion RRWL-atomic time-ns))

(defn throughput-tests
  "Does a series of throughput tests on both CSL and RRWL, and outputs the
  results as a series of plots."
  ([outdir]
     (throughput-tests 2 8 1 0.01))
  ([outdir start iterations times portion-step]
     (println "Doing throughput tests... (this may take a while)")
     (let [get-throughput
           (fn [rwl kw num-players]
             (for [read-portion (range 0 1 portion-step)]
               {:portion read-portion,
                :throughput (incanter.stats/mean
                              (for [_ (range times)]
                                (play-game-throughput num-players
                                                      read-portion
                                                      rwl
                                                      5000000000)))
                :type kw}))]
       (doseq [num-players (util/powers-of 2 start iterations)]
         (let [throughput 
               (incanter.core/to-dataset
                 (concat (get-throughput CSL-atomic  "CSL"  num-players)
                         (get-throughput RRWL-atomic "RRWL" num-players)))]
           (incanter.core/save (incanter.charts/line-chart
                                 :portion  :throughput
                                 :data     throughput
                                 :group-by :type
                                 :title    (str num-players " players"
                                                (if (> times 1)
                                                  (str " -- average of " times 
                                                       " runs")
                                                  ""))
                                 :x-label  "% Readers"
                                 :y-label  "Operations/ms"
                                 :legend   true)
                               (new java.io.File outdir
                                    (str "throughput-"
                                         num-players
                                         "-players"
                                         (if (> times 1)
                                           (str times "-averaged")
                                           "")
                                         ".png")))))
       (println "Done"))))
