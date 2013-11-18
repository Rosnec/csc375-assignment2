(ns rwl.core-test
  {:author "Dan Wysocki"}
  (:use clojure.test)
  (:require [rwl.locks.reentrant-rwl :refer [RRWL-atomic RRWL]]
            [rwl.locks.countdown-semaphore-lock :refer [CSL-atomic CSL]]
            [rwl.util :refer [dopool powers-of intarray-rwl]])
  (:import [java.util.concurrent Executors]))

;; (deftest orl-test
;;   (testing "ORL testing"
;;     (let [m (orl-map)]
;;       (m :write "k" "v")             ; Adds k -> v to the map
;;       (is (= (m :read "k") "v"))     ; Tests that k -> v
;;       (m :write "k" "v2")            ; Replaces k -> v with k -> v2
;;       (is (= (m :read "k") "v2"))))) ; Tests that k -> v2

;; (defn rwl-test [rwl]
;;   (dotimes [_ 10]
;;     (let [counter (agent 0)
;;           step (inc (rand-int 10))
;;           length (* step (quot (rand-int 1000) step))
;;           nthreads 2;(+ 5 (rand-int 5))
;;           test-seq (partition 2 (range 0 length step))
;;           parted (partition-all (quot length (* 2 nthreads))
;;                                 test-seq)
;;           pool (Executors/newFixedThreadPool nthreads)
;;           writes (for [part parted]
;;                    (fn [] (doseq [[key val] part]
;;                             (rwl :write key val))))
;;           reads (for [part parted]
;;                   (fn [] (doseq [[key val] part]
;;                            (send counter inc)
;;                            (is (= (rwl :read key) val)))))]
;;       (doseq [results (.invokeAll pool writes)]
;;         (.get results))
;;       (doseq [results (.invokeAll pool reads)]
;;         (.get results))
;;       (is (= counter (/ length 2)))
;;       (.shutdown pool))))

;; (deftest orl-map-test
;;   (testing "ORL-map test"
;;     (rwl-test (orl-map))))

(defn rwl-consistency-test
  [lock-fn]
  (let [rwl (lock-fn {})
        data (range 0 1000)];(take 1000 (repeatedly #(rand-int 1000)))
;        reads (for [x data] (fn [] (rwl :write assoc x (* 2 x))))
;        writes (for [x data] (fn [] (is (= ((rwl :read) x) (* 2 x)))))
;        nthreads 2
;        pool (Executors/newFixedThreadPool nthreads)]
    ;; (doseq [results (.invokeAll pool writes)]
    ;;   (.get results))
    ;; (doseq [results (.invokeAll pool reads)]
    ;;   (.get results))
    ;; (.shutdown pool)))
   (dorun (pmap (fn [x] (rwl :write assoc x (* 2 x))) data))
;   (println "testvalue: " (rwl :read)) ; stalls here, and fails next test
                                            ; probably fails because it stalls
                                            ; and doesn't return anything as a
                                            ; result
   (dorun (pmap (fn [x] (is (= ((rwl :read) x) (* 2 x)))) data))
   (is (= (count (rwl :read)) (count data)))))
;   (println "testvalue: " (count (rwl :read)))))

(defn rwl-stress-test
  [rwl readers writers]
  (let [rwl (rwl {})
        counter (agent 0)
        total (+ readers writers)
        read-fn  (fn [x] (is (or (= ((rwl :read) x) x)
                                 (= ((rwl :read) x) nil))))
        write-fn (fn [x] (rwl :write assoc x x) (send counter inc))
        readers-writers (shuffle (concat (take readers (repeat read-fn))
                                         (take writers (repeat write-fn))))
        data (take total (shuffle (range (* 1000 total))))]
    (dorun (pmap #(% %2) readers-writers data))
    (await counter)
    (is (= @counter writers))))

(defn rwl-aarray-test
  "Tries filling an array of length N with (range N) by appending the values
  concurrently. The order is not necessarily preserved, but the elements should
  be, so to test it, we sum the elements, and see if it is equal to
  (apply + (range length))."
  [aarr-rwl length threads]
  (let [rwl (aarr-rwl Integer length)
        data (range length)]
    (dopool #(rwl :write :append %) data threads)
    (is (= (apply +' data)
           (apply +' (for [idx data]
                       (rwl :read idx)))))))

(defn rwl-intarray-test
  "Tries filling an array of length N with (range N) by appending the values
  concurrently. The order is not necessarily preserved, but the elements should
  be, so to test it, we sum the elements, and see if it is equal to
  (apply + (range length))."
  [rwl length threads]
  (let [iarr-rwl (intarray-rwl rwl length)
        data (partition-all (/ length threads) (range length))]
    (println "gonna pool dis shit son")
    (dopool #((doseq [x %] (iarr-rwl :write :append x))) data threads)
    (println "pool's closed")
    (is (= (apply +' data)
           (apply +' (for [idx data]
                       (iarr-rwl :read idx)))))))

;; (deftest RRWL-test
;;   (testing "Testing RRWL"
;;     (rwl-consistency-test rwl.locks.reentrant-rwl/RRWL-atomic)))

;; (deftest CSL-atomic-test
;;   (testing "Testing CSL for consistency"
;;     (rwl-consistency-test rwl.locks.countdown-semaphore-lock/CSL-atomic))
;;   (doseq [readers [10 100 1000 10000]
;;           writers [10 100 1000 10000]]
;;     (testing (str "Stress testing CSL-atomic with "
;;                   readers " readers and "
;;                   writers " writers."))
;;     (rwl-stress-test rwl.locks.countdown-semaphore-lock/CSL-atomic
;;                      readers writers)))

(deftest intarray-test
  (testing "Testing RRWL and CSL on an AArray"
    (doseq [rwl [CSL RRWL]]
      (doseq [threads (powers-of 2 2 5)]
        (rwl-intarray-test rwl 50 threads)))))

;(deftest CSL-atomic-RRWL-test
;  (testing "Tests validity of CSL-atomic using a RRWL"

