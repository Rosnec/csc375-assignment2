(ns rwl.locks.reentrant-rwl
  (:require [rwl.locks.interfaces :refer [general-rwl]])
  (:require [rwl.util :refer [atomic-rwl array-rwl aarray-rwl]])
  (:import (java.util.concurrent.locks ReentrantReadWriteLock)))

(defn RRWL
  "A functional abstraction of Java's ReentrantReadWriteLock.
  Implements rwl.locks.interfaces/general-rwl, and calls read-fn while holding
  the read-lock, and write-fn while holding the write-lock."
  [read-fn write-fn]
  (let [rwl (new ReentrantReadWriteLock true)
        read-lock (.readLock rwl)
        write-lock (.writeLock rwl)
        read-mode (fn [& args]
                    (.lock read-lock)
                    (let [x (apply read-fn args)]
                      (.unlock read-lock)
                      x))
        write-mode (fn [& args]
                     (.lock write-lock)
                     (apply write-fn args)
                     (.unlock write-lock))]
    (general-rwl read-mode write-mode)))

;; (defn RRWL-atomic
;;   "Puts an arbitrary piece of data in an Atom, and puts a RRWL on it.
;;   Returns the RRWL interface function.
;;   Options
;;     :read[]           - derefs the atom
;;     :write[func args] - swaps the atom with the result of func applied to it
;;                         with args"
;;   [x]
;;   (let [atomic-x (atom x)
;;         read-fn  (fn [] (deref atomic-x))
;;         write-fn (fn [func args] (apply swap! atomic-x func args))]
;;     (RRWL read-fn write-fn)))

(defn RRWL-atomic
  [x]
  (atomic-rwl RRWL x))

(defn RRWL-array
  [& args]
  (apply array-rwl RRWL args))

(defn RRWL-aarray
  [& args]
  (apply aarray-rwl RRWL args))

;; (defn RRWL-array
;;   "Puts a RRWL on an array, which can either be provided ready-made, or
;;   constructed using the provided parameters.
;;   Returns the RRWL interface function
;;   Options
;;     :read[idx & idxs]  - returns the value at the given index(es)
;;     :write[idx & idxv] - writes the last item in idxv as the value at the given
;;                          index(es)"
;;   ([arr]
;;      (let [read-fn  (fn [idx & idxs] (apply aget arr idx idxs))
;;            write-fn (fn [idx & idxv] (apply aset arr idx idxv))]
;;        (RRWL read-fn write-fn)))
;;   ([type dim & more-dims]
;;      (RRWL-array (apply make-array type dim more-dims))))
