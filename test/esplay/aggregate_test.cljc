(ns esplay.aggregate-test
  (:require [clojure.test :refer :all]
            [esplay.aggregate :as aggregate]
            [esplay.mocks :refer :all]
            [esplay.schemas :as schemas]))

(deftest test-aggregate-add
  (testing "adding an aggregate to an empty store"
    (is (= (assoc schemas/initial-event-store
                  :aggregates {(:id mock-agg-1) mock-agg-1})
           (aggregate/add schemas/initial-event-store [(:id mock-agg-1) mock-agg-1]))))

  (testing "adding an aggregate to a non-empty store"
    (is (= (assoc schemas/initial-event-store
                  :aggregates {(:id mock-agg-1) mock-agg-1
                               (:id mock-agg-2) mock-agg-2})
           (-> schemas/initial-event-store
               (aggregate/add [(:id mock-agg-1) mock-agg-1])
               (aggregate/add [(:id mock-agg-2) mock-agg-2])))))

  (testing "replacing an existing aggregate"
    (is (= (assoc schemas/initial-event-store
                  :aggregates {(:id mock-agg-1a) mock-agg-1a})
           (-> schemas/initial-event-store
               (aggregate/add [(:id mock-agg-1) mock-agg-1])
               (aggregate/add [(:id mock-agg-1a) mock-agg-1a]))))))

(deftest test-aggregate-update
  (testing "updating non-existent aggregate"
    (let [sval' (aggregate/update schemas/initial-event-store :foo (fnil inc 0))]
      (is (= {:foo 1}
             (:aggregates sval')))))

  (testing "updating existent aggregate"
    (let [sval (assoc-in schemas/initial-event-store [:aggregates :foo] 68)
          sval' (aggregate/update sval :foo (fnil inc 0))]
      (is (= {:foo 69}
             (:aggregates sval'))))))

;; (run-tests)
