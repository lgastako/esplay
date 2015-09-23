(ns esplay.projections-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [esplay.core :refer :all]
            [esplay.aggregate :as aggregate]
            [esplay.event :as event]
            [esplay.projection :as projection]
            [esplay.store :as store]
            [its.log :as log]))

(defn project-event-type-count [sval event]
  (let [event-type (:event-type event)
        agg-id (->> event-type
                    ((juxt namespace name))
                    (str/join "/")
                    (str "count:"))]
    (log/debug :project-event-type-count {:sval sval
                                          :event event
                                          :agg-id agg-id})
    (update-in sval [:aggregates agg-id] (fnil inc 0))))

(deftest test-find-new-events
  (testing "explodes if bs smaller than as"
    (is (thrown? AssertionError (projection/find-new-events [:a :b :c] [:a :b]))))

  (testing "explodes if as not the head of bs"
    (is (thrown? AssertionError (projection/find-new-events [:a :b] [:c :d :e]))))

  (testing "returns new events when as and bs are ok"
    (is (= [:c :d]
           (projection/find-new-events [:a :b] [:a :b :c :d])))))

(deftest test-add-projection
  (testing "adding a projection to an empty store"
    (let [sref (store/create)
          projection (fn [sref event])]
      (projection/add sref projection)
      (await sref)
      (is (= [projection]
             (:projections @sref)))))

  (testing "adding a projection adds it at the end"
    (let [sref (store/create)
          projection1 (fn [sref event])
          projection2 (fn [sref event])]
      (projection/add sref projection1)
      (projection/add sref projection2)
      (await sref)
      (is (= [projection1 projection2]
             (:projections @sref))))))

(deftest test-apply-all
  (testing "firing a basic projection"
    (let [sref (store/create)]
      (projection/add sref project-event-type-count)
      (await sref)
      (event/post! sref {:event-type :projection/test})
      (await sref)
      (is (= :foo (:aggregates @sref))))))

(run-tests)
