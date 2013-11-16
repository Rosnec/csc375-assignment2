(ns rwl.core
  {:author "Dan Wysocki"}
  (:require [rwl.star-flight-3d :refer [throughput-tests]]
            [rwl.locks.reentrant-rwl :refer [RRWL-aarray]])
  (:gen-class))

(defn -main
  "Runs throughput tests and outputs to the command-line argument -- outdir"
  ([]
     (println (RRWL-aarray Integer 10)))
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

