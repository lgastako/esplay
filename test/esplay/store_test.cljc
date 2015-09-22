(ns esplay.store-test
  (:require [clojure.test :refer :all]
            [esplay.store :as store]
            [esplay.test-helpers :refer [wait-for-updates]]))

(deftest test-create-store
  (testing "shape of store"
    (let [sref (store/create)
          sval @sref]
      (is (contains? sval :aggregates))
      (is (contains? sval :events))
      (is (contains? sval :indexes))
      (is (contains? sval :projections)))))

;; (run-tests)
