(ns esplay.errors-test
  (:require [clojure.test :refer :all]
            [esplay.errors :as errors]
            [its.log :as log]))

(deftest test-handle-errors
  (testing "error handler logs appropriate values"
    (let [errors (atom [])]
      (with-redefs [log/error #(swap! errors conj (vec %&))]
        (errors/handle :mock-store :mock-ex)
        (is (= [[:errors/handle {:sref :mock-store
                                 :ex :mock-ex}]]
               @errors))))))

;; (run-tests)
