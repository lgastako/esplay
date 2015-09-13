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
    (is (= [:c :d]
           (find-new-events [:a :b] [:a :b :c :d])))))

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
      (is (contains? ref :indexes))
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

(deftest test-add-projection
  (testing "adding a projection to an empty store"
    (let [store (create-store)
          projection (fn [store event])]
      (add-projection store projection)
      (is (= [projection]
             (:projections @store)))))

  (testing "adding a projection adds it at the end"
    (let [store (create-store)
          projection1 (fn [store event])
          projection2 (fn [store event])]
      (add-projection store projection1)
      (add-projection store projection2)
      (is (= [projection1 projection2]
             (:projections @store))))))

(deftest test-index-key [ref key]
  (testing "indexing empty aggregates"
    (let [ref {:aggregates []}
          ref' (index-key ref :foo)]
      (is (= (assoc ref :indexes {:foo {}})
             ref'))))

  (testing "indexing aggregates with non-existent key"
    (let [ref {:aggregates [{:foo :bar}]}
          ref' (index-key ref :baz)]
      (is (= (assoc ref :indexes {:baz {}})
             ref'))))

  (testing "indexing aggregates with existing key"
    (let [ref {:aggregates [{:foo :bar :id 1}
                            {:foo :bar :id 2}
                            {:foo :baz}
                            {:bif :Bam}]}
          ref' (index-key ref :foo)]
      (is (= (assoc ref :indexes {:foo {{:foo :bar} #{{:foo :bar :id 1}
                                                      {:foo :bar :id 2}}
                                        {:foo :baz} #{{:foo :baz}}}})
             ref')))))

(deftest test-update-indexes
  (testing "basic index updates"
    (let [old {:aggregates [{:foo :bar :id 1}]}
          new {:aggregates [{:foo :bar :id 1}
                            {:foo :baz}
                            {:foo :bar :id 2}]}
          ref new
          ref' (update-indexes :n/a ref old new)]
      (is (= {:foo {{:foo :bar} #{{:foo :bar
                                   :id 2}
                                  {:foo :bar
                                   :id 1}}
                    {:foo :baz} #{{:foo :baz}}}
              :id {{:id 1} #{{:foo :bar
                              :id 1}}
                   {:id 2} #{{:foo :bar
                              :id 2}}}}
             (:indexes ref'))))))

(deftest test-all-keys-from
  (testing "no xs"
    (is (= #{}
           (all-keys-from nil))))

  (testing "no keys"
    (is (= #{}
           (all-keys-from [{} {} {}])))))

(deftest test-find-updated-aggregates
  (testing "no aggregates"
    (let [aggs []]
      (is (= []
             (find-updated-aggregates aggs aggs)))))

  (testing "no new aggregates"
    (let [aggs [{:foo :bar}]]
      (is (= []
             (find-updated-aggregates aggs aggs)))))

  (testing "new aggregates"
    (let [old [{:foo :bar}]
          new [{:foo :bar}
               {:baz :bif}
               {:bam :boom}]]
      (is (= [{:baz :bif}
              {:bam :boom}]
             (find-updated-aggregates old new))))))

(deftest test-search
  (testing "zero kvs"
    (let [store (create-store)]
      (is (thrown? AssertionError (search store)))))

  (testing "odd number of kvs"
    (let [store (create-store)]
      (is (thrown? AssertionError (search store)))))

  (testing "a single kv with no results"
    (let [store (create-store)]
      (is (= :fixme
             (search store
                     :username "john")))))

  (testing "a single kv with results"
    (let [store (create-store)]
      (swap! store assoc :aggregates {"john" {:username "john"}})
      (update-indexes nil store {} @store)
      (is (= :fixme
             (search store
                     :username "john")))))

  (testing "multiple kvs with no results"
    (let [store (create-store)]
      (is (= nil (search store
                         :username "john"
                         :something "else")))))

  (testing "multiple kvs with results"))
