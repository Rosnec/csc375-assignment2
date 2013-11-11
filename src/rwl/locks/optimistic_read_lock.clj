(ns rwl.locks.optimistic-read-lock)


(defn- do-read
  [^clojure.lang.IFn  read-fn
                      key
   ^clojure.lang.Atom counter]
  (let [c @counter
        r (apply read-fn key)]
    (if (= c @counter)
      r
      (recur read-fn key counter))))

(defn- do-write
  [^clojure.lang.IFn  write-fn
   ^clojure.lang.ISeq key-value-pair
   ^clojure.lang.Atom counter]
  (dosync
    (if (even? @counter)
      (do
        (swap! counter inc)
        (apply write-fn key-value-pair)
        (swap! counter inc))
      (recur))))

(defn orl
  "Generalized optimistic read lock. Provide a read-fn and write-fn which both
  have access to some shared data structure, and perform read/write operations
  on it. Returns a function which reads and writes to the map. Function can be
  called in 2 ways:
  (f :read key)
  (f :write key value)"
  [^clojure.lang.IFn read-fn
   ^clojure.lang.IFn write-fn]
  (let [counter (atom 0N)]
    (fn [read-or-write & args]
      (cond
        (= read-or-write :read) (do-read read-fn args counter)
        (= read-or-write :write) (do-write write-fn args counter)))))

(defn orl-map
  "Wraps a map in an optimistic read lock. Optionally provide an initial map"
  ([]
    (orl-map {}))
  ([m]
     (let [m (ref m)
           read-fn (fn [key] (@m key))
           write-fn
           (fn [key value] (dosync
                             (alter m assoc key value)))]
       (orl read-fn write-fn))))
                         
