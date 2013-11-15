(defproject csc375-assignment2 "0.3.0"
  :description "A readers-writer locking mechanism library and performance
                testing suite. Implements an original lock, called a
                countdown-semaphore-lock, and compares its performance to
                java.util.concurrent's ReentrantReadWriteLock."
  :url "https://github.com/Rosnec/csc375-assignment2"
  :license {:name "GNU General Public License"
            :url "GNU General Public License"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [incanter "1.5.4"]]
  :main rwl.core
  :test-path "test"
  :jvm-opts ["-Djava.awt.headless=true"])
