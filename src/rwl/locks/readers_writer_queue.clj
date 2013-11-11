(ns rwl.locks.readers-writer-queue
  (:require [rwl.locks.interfaces :refer [general-rwl]])
  (:import (java.util.concurrent.locks ReentrantLock)))

(defn rw-queue
  "Forces reads and writes "
  [x]
  (let [lock            (ReentrantLock.)
        readers         (atom 0)
        writer          (atom 0)
        waiting-readers (atom 0)
        waiting-writers (atom 0)
        atomic-x        (atom x)

        read-mode
        (fn []
          (.lock lock)
          (locking readers
            (while (or (not (zero? writers))
                       (not (zero? waiting-writers)))
              (swap! waiting-readers inc)
              (.wait readers)
              (swap! waiting-readers dec))
            (swap! readers inc)
            (let [deref-x @atomic-x]
              ; implement unlockRead from p26 of notes
          )

        write-mode
        (fn [func args]
          )]
    (general-rwl read-mode write-mode)))
