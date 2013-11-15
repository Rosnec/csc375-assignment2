(ns rwl.locks.countdown-semaphore-lock
  (:require [rwl.locks.interfaces :refer [general-rwl]])
  (:import [java.util.concurrent Semaphore CountDownLatch]))

(defn CSL
  ""
  ([x]
     (CSL x 1000000 false))
  ([x permits]
     (CSL x permits false))
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
             (loop [held-latch nil]
               (.await @writer-latch)
               (let [active-latch @writer-latch]
               (if (locking active-latch
                     (if (= held-latch active-latch)
                       (.tryAcquire reader-P)
                       (if (and (< (.availablePermits reader-P) permits)
                                (zero? @waiting-writers))
                         (.tryAcquire reader-P))))
               ; acquired a permit
                 (let [deref-x @atomic-x]
                   (.release reader-P)
                   deref-x)
               ; did not acquire a permit
                 (recur active-latch))))]
       (general-rwl read-fn write-fn))))
                   
               
