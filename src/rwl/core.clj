(ns rwl.core
  (:require [rwl.star-flight-3d :refer [throughput-tests]])
  (:gen-class))

(defn -main
  "I don't do a whole lot."
  [outdir]
  (throughput-tests outdir))
