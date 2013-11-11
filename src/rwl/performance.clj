(ns rwl.performance
  (:require [incanter core charts stats])
  (:import [java.util.concurrent Executors]))

(defn nano-time
  "Returns time in nanoseconds the function takes to complete"
  [func]
  (let [start-time (System/nanoTime)]
    (func)
    (- (System/nanoTime) start-time)))

(defn micro-time
  "Returns time in microseconds the function takes to complete"
  [func]
  (/ (double (nano-time func)) 1000))

(defn milli-time
  "Returns time in milliseconds the function takes to complete"
  [func]
  (/ (double (nano-time func)) 1000000))

(defn sec-time
  "Returns the time in seconds the function takes to complete"
  [func]
  (/ (double (nano-time func)) 1000000000))

(defn read-write-scenario
  "Returns a dataset of the time in nanoseconds to make each iteration of a
  read or a write, and whether it is a :read or a :write"
  [read-fn write-fn readers writers nthreads]
  (if (or (< readers 0) (< writers 0))
    (throw Exception)
    (let [timed-read-fn (nano-time read-fn)
          rw-count (+ readers writers)
          rw-seq (shuffle (concat (take readers
                                         [(-> read-fn nano-time repeat)
                                          :read])
                                   (take writers
                                         [(-> write-fn nano-time repeat)
                                          :write])))
;          rw-seq (for [[time _] rw-data] time)
;          rw-seq (shuffle (concat (take readers
                                  ;;       (-> read-fn nano-time repeat))
                                  ;; (take writers
                                  ;;       (-> write-fn nano-time repeat))))
          pool (Executors/newFixedThreadPool nthreads)
;          rw-data (pmap .get (.invokeAll pool rw-seq))]
          rw-data (doseq [results (.invokeAll pool rw-seq)] (.get results))]
      (.shutdown pool)
      (incanter.core/dataset [:ms-time :rw] rw-data))))

(defn read-write-one-constant
  ""
  [read-fn write-fn readers writers nthreads]
  (cond ;(and (sequential? readers)
        ;     (sequential? writers))
        ;(throw Exception)

        (and (sequential? readers)
             (number? writers))
        ; VV We need to average this out VV
        (incanter.stats/mean (for [r readers]
                               (read-write-scenario read-fn
                                                    write-fn
                                                    r writers))
        
        (and (number? readers)
             (sequential? writers))
        ; VV We need to average this out VV
        (incanter.stats/mean (for [w writers]
                               (read-write-scenario read-fn
                                                    write-fn
                                                    readers w)))
        :else (throw Exception))))

(defn read-write-chart
  "Creates charts..."
  [dataset & {:keys [title x-axis y-axis]
              :or {:title "Plot", :x-axis "N", :y-axis "time (ms)"}}]
  (incanter.charts/scatter-plot :ms-time :rw
                                :data dataset
                                :group-by :rw
                                :title :title
                                :x-label :x-label
                                :y-label :y-label))

