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
    (is (= (assoc initial-event-store :aggregates
                  {(:id mock-agg-1) mock-agg-1})
           (add-agg initial-event-store [(:id mock-agg-1) mock-agg-1]))))

  (testing "adding an aggregate to a non-empty store"
    (is (= (assoc initial-event-store :aggregates
                  {(:id mock-agg-1) mock-agg-1
                   (:id mock-agg-2) mock-agg-2})
           (-> initial-event-store
               (add-agg [(:id mock-agg-1) mock-agg-1])
               (add-agg [(:id mock-agg-2) mock-agg-2])))))

  (testing "replacing an existing aggregate"
    (is (= (assoc initial-event-store :aggregates
                  {(:id mock-agg-1a) mock-agg-1a})
           (-> initial-event-store
               (add-agg [(:id mock-agg-1) mock-agg-1])
               (add-agg [(:id mock-agg-1a) mock-agg-1a]))))))

(deftest test-create-store
  (testing "shape of store"
    (let [store (create-store)
          ref @store]
      (is (contains? ref :aggregates))
      (is (contains? ref :events))
      (is (contains? ref :indices))
      (is (contains? ref :projections)))))

(deftest test-post-event!
  (testing "posting event to an empty store"
    (let [store (create-store)]
      (post-event! store {:foo :bar})
      (is (= [{:foo :bar}]
             (:events @store)))))

  (testing "events are posted in order"
    (let [store (create-store)]
      (doseq [n (range 10)]
        (post-event! store n))
      (is (= [0 1 2 3 4 5 6 7 8 9]
             (:events @store))))))
