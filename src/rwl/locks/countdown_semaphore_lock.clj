(ns rwl.locks.countdown-semaphore-lock
  {:author "Dan Wysocki"}
  (:require [rwl.locks.interfaces :refer [general-rwl]])
  (:require [rwl.util :refer [atomic-rwl array-rwl aarray-rwl]])
  (:import [java.util.concurrent Semaphore CountDownLatch]))

(defn CSL
  ""
  ([read-fn write-fn]
     (CSL read-fn write-fn 100000 false))
  ([read-fn write-fn permits]
     (CSL read-fn write-fn permits false))
  ([read-fn write-fn permits fair?]
     (let [reader-P (new Semaphore permits fair?) ; reader permits
           writer-latch (atom (new CountDownLatch 0))
           waiting-writers (atom 0N)
           write-mode (fn [& args]
                        (loop [new-latch (new CountDownLatch 1)]
                          (swap! waiting-writers inc)
                          (.await @writer-latch)
                          (swap! waiting-writers dec)
                          (if (= new-latch
                                 (locking reader-P
                                   (if (= permits (.availablePermits reader-P))
                                     (locking writer-latch
                                       (if (zero? (.getCount @writer-latch))
                                         (reset! writer-latch new-latch))))))
                            (do (apply write-fn args)
                                (.countDown @writer-latch))
                            (recur new-latch))))
           read-mode (fn [& args]
                       (loop [held-latch nil]
                         (.await @writer-latch)
                         (let [active-latch @writer-latch]
                           (if (locking active-latch
                                 (if (= held-latch active-latch)
                                   (.tryAcquire reader-P)
                                   (if (and (< (.availablePermits reader-P)
                                               permits)
                                            (zero? @waiting-writers))
                                     (.tryAcquire reader-P))))
                           ; acquired a permit
                             (let [x (apply read-fn args)]
                               (.release reader-P)
                               x)
                           ; did not acquire a permit
                             (recur active-latch)))))]
       (general-rwl read-mode write-mode))))

(defn CSL-atomic
  [x]
  (atomic-rwl CSL x))

(defn CSL-array
  [args]
  (apply array-rwl CSL args))

(defn CSL-aarray
  [args]
  (apply aarray-rwl CSL args))
