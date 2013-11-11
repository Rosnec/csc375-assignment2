(ns rwl.locks.smart-lock
  (:require rwl.locks.interfaces))




;; (defn mode-of-operation
;;   ([P waiting-readers waiting-writers]
;;      (mode-of-operation P waiting-readers waiting-writers 0))
;;   ([P waiting-readers waiting-writers bias]
;;      (- (+ waiting-readers bias)
;;         waiting-writers P)))

;; (defn counting-semaphore-lock
;;   ([^clojure.lang.IFn data-reader
;;     ^clojure.lang.IFn data-writer]
;;      (counting-semaphore-lock data-reader data-writer 0))
;;   ([^clojure.lang.IFn data-reader
;;     ^clojure.lang.IFn data-writer
;;     ^java.lang.Number bias]
;;      (let [P               (atom  0)
;;            bias            (atom  bias)
;;            waiting-readers (agent [])
;;            waiting-writers (agent [])
;;            read-fn
;;            (fn [key] (if
;;                          (< (mode-of-operation P
;;                                                waiting-readers
;;                                                waiting-writers
;;                                                bias)
;;                             0)
;;                        (
;;                        (dosync
;;                         (if (>= (mode-of-operation P
;;                                                    waiting-readers
;;                                                    waiting-writers
;;                                                    bias)
;;                                 0)
                          
;;                        (zero? P) (if (compare-and-set! P 0 1)
;;                                    (do
;;                                      (send wai
                         
;;        (rwl.locks.interfaces/general-rwl 
