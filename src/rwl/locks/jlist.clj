(ns rwl.locks.jlist
  (:require rwl.locks.interfaces)
  (:import [java.util.concurrent ConcurrentLinkedQueue]))

(defn jlinked-queue
  ([]
     (jlinked-queue []))
  ([^java.util.Collection col]
     (let [queue (new ConcurrentLinkedQueue col)
           read-fn #()
           write-fn #()]
       (rwl.locks.interfaces/general-rwl read-fn write-fn))))
