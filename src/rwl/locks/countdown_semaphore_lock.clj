(ns rwl.locks.countdown-semaphore-lock
  (:require [rwl.locks.interfaces :refer [general-rwl]])
  (:import [java.util.concurrent Semaphore CountDownLatch]))

(defn CSL
  ""
  ([x]
     (dsl x 1000000 false))
  ([x permits]
     (dsl x permits false))
  ([x permits fair?]
     (let [atomic-x (atom x)
           reader-P (new Semaphore permits fair?) ; reader permits
           writer-latch (atom (new CountDownLatch 0))
           waiting-writers (atom 0N)

           write-fn
           (fn [func args]
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
                 (do (apply swap! atomic-x func args)
                     (.countDown @writer-latch))
                 (recur new-latch))))

           read-fn
           (fn []
             (let [held-latch @writer-latch]
               (.await held-latch)
               (if (locking writer-latch
                     (if (= held-latch @writer-latch)
                       (.tryAcquire reader-P)
                       (if (and (< (.availablePermits reader-P) permits)
                                (zero? @waiting-writers))
                         (.tryAcquire reader-P))))
               ; acquired a permit
                 (let [deref-x @atomic-x]
                   (.release reader-P)
                   deref-x)
               ; did not acquire a permit
                 (recur))))]
       (general-rwl read-fn write-fn))))
                   
               
