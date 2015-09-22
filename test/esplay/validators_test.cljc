(ns esplay.validators-test
  (:require [clojure.test :refer :all]
            [esplay.schemas :as schemas]
            [esplay.validators :as validators]))

(deftest test-validate-sval
  (testing "a valid sval"
    (is (validators/sval schemas/initial-event-store)))

  (testing "invalid svals"
    (is (thrown? Exception (validators/sval (dissoc schemas/initial-event-store :aggregates))))
    (is (thrown? Exception (validators/sval (dissoc schemas/initial-event-store :events))))
    (is (thrown? Exception (validators/sval (dissoc schemas/initial-event-store :index))))
    (is (thrown? Exception (validators/sval (dissoc schemas/initial-event-store :projections))))
    (is (thrown? Exception (validators/sval {:foo :bar})))
    (is (thrown? Exception (validators/sval {})))
    (is (thrown? Exception (validators/sval nil)))))

;; (run-tests)
