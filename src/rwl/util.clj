(ns rwl.util
  (:import [java.util.concurrent Executors]))

(defn powers-of
  "Returns an infinite lazy sequence of the powers of n
  Taken from http://stackoverflow.com/a/13066466/890705"
  [n]
  (iterate (partial *' n)))

(defn prob
  [probability true-fn false-fn & args]
  (apply (if (< (rand) probability)
           true-fn
           false-fn)
         args))

(defn pool
  "Returns an ExecutorService fixed thread pool"
  [threads]
  (Executors/newFixedThreadPool threads))

(defn dopool
  "Executes a function over a collection using a fixed thread pool"
  [func coll threads]
  (let [thread-pool (pool threads)
        func-coll (for [item coll]
                    #(func item))]
    (doseq [results (.invokeAll thread-pool func-coll)]
      (.get results))
    (.shutdown thread-pool)))

