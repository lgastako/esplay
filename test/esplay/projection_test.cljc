(ns esplay.projection-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [esplay.core :refer :all]
            [esplay.aggregate :as aggregate]
            [esplay.event :as event]
            [esplay.projection :as projection]
            [esplay.schemas :as schemas]
            [esplay.store :as store]
            [its.log :as log]))

(defn project-event-type-count [sval event]
  (let [event-type (:event-type event)
        agg-id (some->> event-type
                        ((juxt namespace name))
                        (str/join "/")
                        (str "count:"))]
    (if-not agg-id
      (log/error :failed-to-calc-agg-id {:event event})
      (let [f (fnil inc 0)
            agg (-> sval
                    (get-in [:aggregates agg-id])
                    f)]
        [[agg-id agg]]))))

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

(deftest test-project-sval
  (testing "..."
    (let [event {:event-type :foo}
          sval schemas/initial-event-store
          sval' (projection/project-sval sval project-event-type-count event)]
      (is (= (assoc sval
                    :aggregates {"count:/foo" 1})
             sval')))))

(deftest test-project
  (testing "..."
    :not-implemented))

(deftest test-apply-all
  (testing "firing a basic projection"
    (let [sref (store/create)]
      (projection/add sref project-event-type-count)
      (await sref)
      (event/post! sref {:event-type :projection/test})
      (await sref)
      (is (= :foo (:aggregates @sref))))))

;; (run-tests)
