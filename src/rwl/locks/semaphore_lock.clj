(ns rwl.locks.semaphore-lock
  (:require rwl.locks.interface :refer [general-rwl]))

; When waiting, put agent in queue
; Return agent
; read-fn derefs agent
(defn counting-semaphore-lock
  ([^clojure.lang.IFn data-reader
    ^clojure.lang.IFn data-writer]
     (counting-semaphore-lock data-reader data-writer 0))
  ([^clojure.lang.IFn data-reader
    ^clojure.lang.IFn data-writer
    ^java.lang.Number bias]
     (let [P (ref 0)
;          unlock using
;          (locking waiting-readers
;            (.notifyAll waiting-readers))
           waiting-reader-lock (Object.)
;          unlock using
;          (locking waiting-writers
;            (.notify waiting-writers))
           waiting-writers (Object.)
           read-fn
           (fn [key]
             (if (< @P 0)
               (locking waiting-reader-lock
                 (.wait waiting-reader-lock)))
             (dosync (if (>= @P 0)
                       (do
                         (alter! P inc)
                         (
             
