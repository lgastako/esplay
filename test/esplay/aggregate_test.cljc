(ns esplay.aggregate-test
  (:require [clojure.test :refer :all]
            [esplay.aggregate :as aggregate]
            [esplay.core :refer :all]
            [esplay.mocks :refer :all]
            [esplay.schemas :refer [initial-event-store]]))

(deftest test-aggregate-add
  (testing "adding an aggregate to an empty store"
    (is (= (assoc initial-event-store :aggregates
                  {(:id mock-agg-1) mock-agg-1})
           (aggregate/add initial-event-store [(:id mock-agg-1) mock-agg-1]))))

  (testing "adding an aggregate to a non-empty store"
    (is (= (assoc initial-event-store :aggregates
                  {(:id mock-agg-1) mock-agg-1
                   (:id mock-agg-2) mock-agg-2})
           (-> initial-event-store
               (aggregate/add [(:id mock-agg-1) mock-agg-1])
               (aggregate/add [(:id mock-agg-2) mock-agg-2])))))

  (testing "replacing an existing aggregate"
    (is (= (assoc initial-event-store :aggregates
                  {(:id mock-agg-1a) mock-agg-1a})
           (-> initial-event-store
               (aggregate/add [(:id mock-agg-1) mock-agg-1])
               (aggregate/add [(:id mock-agg-1a) mock-agg-1a]))))))

;; (run-tests)
