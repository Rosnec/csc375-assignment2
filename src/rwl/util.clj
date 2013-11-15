(ns rwl.util
  {:author "Dan Wysocki"}
  (:import [java.util.concurrent Executors]))

(defn powers-of
  "Returns an infinite lazy sequence of the powers of n, beginning with start,
  unless the number of iterations is specified.
  Taken from http://stackoverflow.com/a/13066466/890705"
  ([n]
     (powers-of n n))
  ([n start]
     (iterate (partial *' n) start))
  ([n start iterations]
     (take iterations (powers-of n start))))

(defn prob-general
  "This is the good, generalized version of prob, but I need to get this working
  *NOW*"
  [probability true-fn false-fn & args]
  (apply (if (< (rand) probability)
           true-fn
           false-fn)
         args))

(defn prob
  "This is the bad, ungeneralized version of prob. It works in this case,
  though, since true-fn is always read and false-fn is always write."
  [probability true-fn false-fn & args]
  (if (< (rand) probability)
    (true-fn)
    (apply false-fn args)))


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

