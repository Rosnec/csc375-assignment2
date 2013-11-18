(ns rwl.core-test
  {:author "Dan Wysocki"}
  (:use clojure.test)
  (:require [rwl.locks.reentrant-rwl :refer [RRWL-atomic RRWL]]
            [rwl.locks.countdown-semaphore-lock :refer [CSL-atomic CSL]]
            [rwl.util :refer [dopool powers-of intarray-rwl]])
  (:import [java.util.concurrent Executors]))


(defn rwl-consistency-test
  [lock-fn]
  (let [rwl (lock-fn {})
        data (range 0 1000)]
   (dorun (pmap (fn [x] (rwl :write assoc x (* 2 x))) data))
   (dorun (pmap (fn [x] (is (= ((rwl :read) x) (* 2 x)))) data))
   (is (= (count (rwl :read)) (count data)))))

(defn rwl-stress-test
  [rwl readers writers]
  (let [rwl      (rwl {})
        counter  (agent 0)
        total    (+ readers writers)
        read-fn  (fn [x] (is (or (= ((rwl :read) x) x)
                                 (= ((rwl :read) x) nil))))
        write-fn (fn [x] (rwl :write assoc x x) (send counter inc))
        readers-writers (shuffle (concat (take readers (repeat read-fn))
                                         (take writers (repeat write-fn))))
        data     (take total (shuffle (range (* 1000 total))))]
    (dorun (pmap #(% %2) readers-writers data))
    (await counter)
    (is (= @counter writers))))

(defn rwl-intarray-test
  "Tries filling an array of length N with (range N) by appending the values
  concurrently. The order is not necessarily preserved, but the elements should
  be, so to test it, we sum the elements, and see if it is equal to
  (apply + (range length))."
  [rwl length threads]
  (let [iarr-rwl (intarray-rwl rwl length)
        data (range length)]
    (dopool #(iarr-rwl :write :append %) data threads)
    (is (= (apply +' data)
           (apply +' (for [idx data]
                       (iarr-rwl :read idx)))))))

(deftest RRWL-test
  (testing "Testing RRWL"
    (rwl-consistency-test rwl.locks.reentrant-rwl/RRWL-atomic)))

(deftest CSL-atomic-test
  (testing "Testing CSL for consistency"
    (rwl-consistency-test rwl.locks.countdown-semaphore-lock/CSL-atomic))
  (doseq [readers [10 100 1000 10000]
          writers [10 100 1000 10000]]
    (testing (str "Stress testing CSL-atomic with "
                  readers " readers and "
                  writers " writers."))
    (rwl-stress-test rwl.locks.countdown-semaphore-lock/CSL-atomic
                     readers writers)))

(deftest intarray-test
  (testing "Testing RRWL and CSL on an IntArray"
    (doseq [rwl [CSL RRWL]]
      (doseq [threads (powers-of 2 2 9)]
        (rwl-intarray-test rwl 10000 threads)))))
