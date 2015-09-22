(ns esplay.validators-test
  (:require [clojure.test :refer :all]
            [esplay.schemas :as schemas]
            [esplay.validators :as validators]))

(deftest test-validate-sval
  (testing "a valid sval"
    (is (validators/sval {:foo :bar}))))
