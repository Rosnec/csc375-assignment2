(ns rwl.core
  {:author "Dan Wysocki"}
  (:require [rwl.star-flight-3d :refer [throughput-tests]])
  (:gen-class))

(defn -main
  "Runs throughput tests and outputs to the command-line argument -- outdir"
  ([outdir]
     (throughput-tests outdir 2 9 0.01))
  ([outdir start]
     (throughput-tests outdir start 9 0.01))
  ([outdir start iterations]
     (throughput-tests outdir start iterations 0.01)))


