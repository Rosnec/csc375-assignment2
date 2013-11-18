(ns rwl.util
  {:author "Dan Wysocki"}
  (:import [java.util.concurrent Executors]
           [util.java AArray]
           [util.java IntArray]))

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

(defn atomic-rwl
  "Puts an arbitrary piece of data in an Atom, and puts a rwl on it.
  Returns the rwl interface function.
  Options
    :read[]           - derefs the atom
    :write[func args] - swaps the atom with the result of func applied to it
                        with args"
  [rwl x]
  (let [atomic-x (atom x)
        read-fn  (fn [] (deref atomic-x))
        write-fn (fn [func & args] (apply swap! atomic-x func args))]
    (rwl read-fn write-fn)))

(defn array-rwl
  "Puts a rwl on an array, which can either be provided ready-made, or
  constructed using the provided parameters.
  Returns the rwl interface function
  Options
    :read[idx & idxs]  - returns the value at the given index(es)
    :write[idx & idxv] - writes the last item in idxv as the value at the given
                         index(es)"
  ([rwl arr]
     (let [read-fn  (fn [idx & idxs] (apply aget arr idx idxs))
           write-fn (fn [idx & idxv] (apply aset arr idx idxv))]
;           write-fn (fn [mode & args]
;                      (cond
;                        (= mode :set) (apply aset arr idx idxv)
;                        (= mode :append) (
       (rwl read-fn write-fn)))
  ([rwl type dim & more-dims]
     (array-rwl rwl (apply make-array type dim more-dims))))

(defn aarray-rwl
  ""
  ([rwl ^util.java.AArray arr]
     (let [read-fn  (fn [idx] (.get arr (int idx)))
           write-fn (fn [mode & args]
                      (cond
                        (= mode :set) (apply #(.set arr %) args)
                        (= mode :append) (apply #(.append arr %) args)))]
       (rwl read-fn write-fn)))
  ([rwl type length]
     (aarray-rwl rwl (new AArray type (int length)))))

(defn intarray-rwl
  ""
  [rwl ^java.lang.Integer length]
  (let [arr (new IntArray length)
        read-mode (fn [idx] (.get arr (int idx)))
        write-mode (fn [mode & args]
                     (cond
                       (= mode :set)    (apply #(.set arr % %2) args)
                       (= mode :append) (apply #(.append arr %) args)))]
    (rwl read-mode write-mode)))
