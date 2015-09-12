(ns esplay.core-test
  (:require [clojure.test :refer :all]
            [esplay.core :refer :all]))

(def mock-agg-1 {:id "fake-id-1" :data "misc"})
(def mock-agg-1a {:id "fake-id-1" :data "misc-a"})
(def mock-agg-2 {:id "fake-id-2" :data "misc-2"})

(deftest test-find-new-events
  (testing "explodes if bs smaller than as"
    (is (thrown? AssertionError (find-new-events [:a :b :c] [:a :b]))))

  (testing "explodes if as not the head of bs"
    (is (thrown? AssertionError (find-new-events [:a :b] [:c :d :e]))))

  (testing "returns new events when as and bs are ok"
    (is (= [:c :d] (find-new-events [:a :b] [:a :b :c :d])))))

(deftest test-add-agg
  (testing "adding an aggregate to an empty store"
    (is (= {:events []
            :projections []
            :aggregates {(:id mock-agg-1) mock-agg-1}}
           (add-agg initial-event-store [(:id mock-agg-1) mock-agg-1]))))

  (testing "adding an aggregate to a non-empty store"
    (is (= {:events []
            :projections []
            :aggregates {(:id mock-agg-1) mock-agg-1
                         (:id mock-agg-2) mock-agg-2}}
           (-> initial-event-store
               (add-agg [(:id mock-agg-1) mock-agg-1])
               (add-agg [(:id mock-agg-2) mock-agg-2])))))

  (testing "replacing an existing aggregate"
    (is (= {:events []
            :projections []
            :aggregates {(:id mock-agg-1a) mock-agg-1a}}
           (-> initial-event-store
               (add-agg [(:id mock-agg-1) mock-agg-1])
               (add-agg [(:id mock-agg-1a) mock-agg-1a]))))))
