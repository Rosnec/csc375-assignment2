(ns rwl.star-flight-3d
  "A 3d space flight game, where players take control of a ship which can freely
  go in any direction in 3d space. Player locations are kept track of in a
  hashmap, mapping player number to [x,y,z] coordinates. Each player has their
  own thread, and so a readers-writer lock is put on the map. In this
  performance test, we compare two different readers-writer locks' performance.
  To focus our measurements only on lock performance, we disable collision
  detection, and so threads simply move their player, or take a snapshot of the
  map at a given time. The percentage of the time that each event happens is
  fixed for a full set of tests, and then incremented."
  (:require [rwl.performance :as performance]
            [rwl.util :as util]
            [rwl.locks.countdown-semaphore-lock :refer [CSL]]
            [rwl.locks.reentrant-rwl :refer [RRWL]]))

(defn play-game
  [num-players read-portion read-fn write-fn]
  (let [player-action #(util/prob read-portion read-fn write-fn %)]
    (dopool player-action (range num-players) num-players)))

(defn play-game-thoroughput
  "Creates a pool of players which continually run until time-ns has elapsed.
  The counter argument must be an agent which you have external access to.
  The thoroughput will be whatever the final value of counter per time-ns."
  ([num-players read-portion read-fn write-fn
    ^clojure.lang.Agent counter]
     (play-game-thoroughput num-players read-portion
                            read-fn write-fn counter 6000000000))
  ([num-players read-portion read-fn write-fn
    ^clojure.lang.Agent counter ^java.lang.Long time-ns]
     (let [end-time (future (+ (System/nanoTime) time-ns))
           [read-fn-t write-fn-t] (for [func [read-fn write-fn]]
                                    (fn [& args] (apply func args)
                                                 (if (< (System/nanoTime)
                                                        @end-time)
                                                   (do (send counter inc)
                                                       (recur args)))))]
       (play-game num-players read-portion read-fn-t write-fn-t))))
