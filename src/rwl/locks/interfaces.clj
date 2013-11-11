(ns rwl.locks.interfaces)

(defn less-general-rwl
  [^clojure.lang.IFn read-fn
   ^clojure.lang.IFn write-fn]
  (fn [command & args]
    (cond
      (= command :read) (read-fn args)
      (= command :write) (write-fn args))))

(defn general-rwl
  [^clojure.lang.IFn read-mode
   ^clojure.lang.IFn write-mode]
  (fn [mode & [func & args]]
    (cond
      (= mode :read) (read-mode)
      (= mode :write) (write-mode func args))))
