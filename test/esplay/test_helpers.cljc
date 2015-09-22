(ns esplay.test-helpers)

(defn wait-for-updates
  ([]   (wait-for-updates 100))
  ([ms] (Thread/sleep ms)))
