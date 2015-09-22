(ns esplay.projections-test
  (:require [clojure.test :refer :all]
            [esplay.core :refer :all]
            [esplay.schemas :refer :all]
            [esplay.projection :refer :all]
            [esplay.test-helpers :refer [wait-for-updates]]))

(deftest test-find-new-events
  (testing "explodes if bs smaller than as"
    (is (thrown? AssertionError (index/find-new-events [:a :b :c] [:a :b]))))

  (testing "explodes if as not the head of bs"
    (is (thrown? AssertionError (index/find-new-events [:a :b] [:c :d :e]))))

  (testing "returns new events when as and bs are ok"
    (is (= [:c :d]
           (find-new-events [:a :b] [:a :b :c :d])))))

(deftest test-add-projection
  (testing "adding a projection to an empty store"
    (let [sref (create-store)
          projection (fn [sref event])]
      (add-projection sref projection)
      (wait-for-updates)
      (is (= [projection]
             (:projections @sref)))))

  (testing "adding a projection adds it at the end"
    (let [sref (create-store)
          projection1 (fn [sref event])
          projection2 (fn [sref event])]
      (add-projection sref projection1)
      (add-projection sref projection2)
      (wait-for-updates)
      (is (= [projection1 projection2]
             (:projections @sref))))))

;; (run-tests)