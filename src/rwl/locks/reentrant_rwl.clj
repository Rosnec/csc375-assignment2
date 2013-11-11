(ns rwl.locks.reentrant-rwl
  (:require [rwl.locks.interfaces :refer [general-rwl]])
  (:import (java.util.concurrent.locks ReentrantReadWriteLock)))

(defn RRWL
  "A functional abstraction of Java's ReentrantReadWriteLock

  Example:
  (let [m "
  [x]
  (let [rwl (new ReentrantReadWriteLock true)
        read-lock (.readLock rwl)
        write-lock (.writeLock rwl)
        atomic-x (atom x)

        read-mode
        (fn []
 ;         (-> rwl .readLock .lock)
          (.lock read-lock)
          (let [deref-x @atomic-x]
;            (-> rwl .readLock .unlock)
            (.unlock read-lock)
            deref-x))

        write-mode
        (fn [func args]
;          (-> rwl .writeLock .lock)
          (.lock write-lock)
          (apply swap! atomic-x func args)
;          (-> rwl .writeLock .unlock))]
          (.unlock write-lock))]
;          (try
;            (apply swap! atomic-x func args)
;            (finally (.unlock write-lock))))]
    (general-rwl read-mode write-mode)))
