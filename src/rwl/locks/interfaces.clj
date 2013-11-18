(ns rwl.locks.interfaces)

(defn general-rwl
  [^clojure.lang.IFn read-mode
   ^clojure.lang.IFn write-mode]
  (fn [mode & args]
    (apply 
      (cond
        (= mode :read) read-mode
        (= mode :write) write-mode)
      args)))
