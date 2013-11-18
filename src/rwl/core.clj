(ns rwl.core
  {:author "Dan Wysocki"}
  (:require [rwl.star-flight-3d :refer [throughput-tests]]
            [rwl.locks.reentrant-rwl :refer [RRWL]]
            [rwl.util :refer [dopool intarray-rwl]])
  (:import [util.java IntArray])
  (:gen-class))

(defn -main
  "Runs throughput tests and outputs to the command-line argument -- outdir"
  ([]
     (let [arr (new IntArray 1000)
           data (range 1000)]
       (dopool #(.append arr %) data 100)
       (println "data" (apply + data)
                "arr" (apply + (for [idx data]
                                 (.get arr idx)))))
     (let [rrwl (intarray-rwl RRWL 100)
           data (range 100)]
       (doseq [idx data]
         (rrwl :write :set idx idx))
       (println "data" (apply + data)
                "arr" (apply + (for [idx data]
                                 (rrwl :read idx))))))
  ([outdir]
     (throughput-tests outdir 2 9 0.01))
  ([outdir start]
     (-main outdir start 9 0.01))
  ([outdir start iterations]
     (-main outdir start iterations 1 0.01))
  ([outdir start iterations times]
     (-main outdir start iterations times 0.01))
  ([outdir start iterations times portion-step]
     (throughput-tests outdir (Integer. start)
                              (Integer. iterations)
                              (Integer. times)
                              (Double. portion-step))))

