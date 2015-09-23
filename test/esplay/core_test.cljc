(ns esplay.core-test
  (:require [clojure.test :refer :all]
            [esplay.core :refer :all]
            [its.log :as log]))

(deftest log-level-is-error
  (is (= :error (log/level))))

;; (run-tests)
