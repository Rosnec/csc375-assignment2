(ns rwl.core
  {:author "Dan Wysocki"}
  (:require [rwl.star-flight-3d :refer [throughput-tests]])
  (:gen-class))

(defn -main
  "Runs throughput tests and outputs to the command-line argument -- outdir"
  ([outdir]
     (throughput-tests outdir 2 9 0.01))
  ([outdir start]
     (-main outdir start 9 0.01))
  ([outdir start iterations]
     (-main outdir start iterations 0.01))
  ([outdir start iterations portion-step]
     (throughput-tests outdir (Integer. start)
                              (Integer. iterations)
                              (Double. portion-step))))

