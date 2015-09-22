(ns esplay.core-test
  (:require [clojure.test :refer :all]
            [esplay.core :refer :all]
            [esplay.schemas :refer :all]
            [esplay.test-helpers :refer [wait-for-updates]]))

(def mock-agg-1  {:id "fake-id-1" :data "misc"})
(def mock-agg-1a {:id "fake-id-1" :data "misc-a"})
(def mock-agg-2  {:id "fake-id-2" :data "misc-2"})

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
    (let [sref (create-store)
          sval @sref]
      (is (contains? sval :aggregates))
      (is (contains? sval :events))
      (is (contains? sval :indexes))
      (is (contains? sval :projections)))))

(deftest test-post-event!
  (testing "posting event to an empty store"
    (let [sref (create-store)]
      (post-event! sref {:event-type :foo})
      (wait-for-updates)
      (is (= [{:event-type :foo}]
             (:events @sref)))))

  (testing "events are posted in order"
    (letfn [(make-event [n]
              {:event-type :n
               :args {:n n}})]
      (let [sref (create-store)]
        (doseq [n (range 10)]
          (post-event! sref (make-event n))
          (wait-for-updates))
        (is (= (map make-event (range 10))
               (:events @sref)))))))

;; (run-tests)
