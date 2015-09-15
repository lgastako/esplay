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
    (let [sref (create-store)
          sval @sref]
      (is (contains? sval :aggregates))
      (is (contains? sval :events))
      (is (contains? sval :indexes))
      (is (contains? sval :projections)))))

(deftest test-post-event!
  (testing "posting event to an empty store"
    (let [sref (create-store)]
      (post-event! sref {:foo :bar})
      (is (= [{:foo :bar}]
             (:events @sref)))))

  (testing "events are posted in order"
    (let [sref (create-store)]
      (doseq [n (range 10)]
        (post-event! sref n))
      (is (= [0 1 2 3 4 5 6 7 8 9]
             (:events @sref))))))

(deftest test-add-projection
  (testing "adding a projection to an empty store"
    (let [sref (create-store)
          projection (fn [sref event])]
      (add-projection sref projection)
      (is (= [projection]
             (:projections @sref)))))

  (testing "adding a projection adds it at the end"
    (let [sref (create-store)
          projection1 (fn [sref event])
          projection2 (fn [sref event])]
      (add-projection sref projection1)
      (add-projection sref projection2)
      (is (= [projection1 projection2]
             (:projections @sref))))))

(deftest test-index-key
  (testing "indexing empty aggregates"
    (let [val {:aggregates []}
          val' (index-key val :foo)]
      (is (= (assoc val :indexes {:foo {}})
             val'))))

  (testing "indexing aggregates with non-existent key"
    (let [val {:aggregates [{:foo :bar}]}
          val' (index-key val :baz)]
      (is (= (assoc val :indexes {:baz {}})
             val'))))

  (testing "indexing aggregates with existing key"
    (let [val {:aggregates [{:foo :bar :id 1}
                            {:foo :bar :id 2}
                            {:foo :baz}
                            {:bif :Bam}]}
          val' (index-key val :foo)]
      (is (= (assoc val :indexes {:foo {{:foo :bar} #{{:foo :bar :id 1}
                                                      {:foo :bar :id 2}}
                                        {:foo :baz} #{{:foo :baz}}}})
             val')))))

(deftest test-update-indexes
  (testing "basic index updates"
    (let [old {:aggregates [{:foo :bar :id 1}]}
          new {:aggregates [{:foo :bar :id 1}
                            {:foo :baz}
                            {:foo :bar :id 2}]}
          val new
          val' (update-indexes :n/a val old new)]
      (is (= {:foo {{:foo :bar} #{{:foo :bar
                                   :id 2}
                                  {:foo :bar
                                   :id 1}}
                    {:foo :baz} #{{:foo :baz}}}
              :id {{:id 1} #{{:foo :bar
                              :id 1}}
                   {:id 2} #{{:foo :bar
                              :id 2}}}}

             (:indexes val'))))))

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

;; (deftest test-search
;;   (testing "zero kvs"
;;     (let [sref (create-store)]
;;       (is (thrown? AssertionError (search sref)))))

;;   (testing "odd number of kvs"
;;     (let [sref (create-store)]
;;       (is (thrown? AssertionError (search sref)))))

;;   (testing "a single kv with no results"
;;     (let [sref (create-store)]
;;       (is (= :fixme
;;              (search sref
;;                      :username "john")))))

;;   (testing "a single kv with results"
;;     (let [sref (create-store)]
;;       (swap! sref assoc :aggregates {"john" {:username "john"}})
;;       (update-indexes nil sref {} @sref)
;;       (is (= :fixme
;;              (search sref
;;                      :username "john")))))

;;   (testing "multiple kvs with no results"
;;     (let [sref (create-store)]
;;       (is (= nil (search sref
;;                          :username "john"
;;                          :something "else")))))

;;   (testing "multiple kvs with multiple hits on a single result"
;;     (let [sref (create-store)]
;;       ;; TODO: create a user with username john and "something: else"
;;       (is (= nil (search sref
;;                          :username "john"
;;                          :something "else")))))

;;   (testing "multiple kvs with multiple results"
;;     (let [sref (create-store)]
;;       ;; TODO: create a user with username john,
;;       ;; a different one with "something: else", etc.
;;       (is (= nil (search sref
;;                          :username "john"
;;                          :something "else"))))))
