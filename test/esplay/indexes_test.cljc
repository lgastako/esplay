(ns esplay.indexes-test
  (:require [clojure.test :refer :all]
            [esplay.core :refer :all]
            [esplay.schemas :refer :all]))

(def mock-agg-1  {:id "fake-id-1" :data "misc"})
(def mock-agg-1a {:id "fake-id-1" :data "misc-a"})
(def mock-agg-2  {:id "fake-id-2" :data "misc-2"})

(deftest test-find-new-events
  (testing "explodes if bs smaller than as"
    (is (thrown? AssertionError (find-new-events [:a :b :c] [:a :b]))))

  (testing "explodes if as not the head of bs"
    (is (thrown? AssertionError (find-new-events [:a :b] [:c :d :e]))))

  (testing "returns new events when as and bs are ok"
    (is (= [:c :d]
           (find-new-events [:a :b] [:a :b :c :d])))))

(deftest test-all-keys-from
  (testing "no xs"
    (is (= #{}
           (all-keys-from nil))))

  (testing "no keys"
    (is (= #{}
           (all-keys-from [{} {} {}]))))

  (testing "some keys"
    (is (= #{:foo :id}
           (all-keys-from [{:foo :bar} {:foo :bar :id 2}])))))

(deftest test-create-indexes
  (testing "basic index creation"
    (let [x (agent {:aggregates {:agg1 {:foo :bar
                                        :baz :bif}
                                 :agg2 {:foo :bar
                                        :baz :boom}
                                 :agg3 {:foo :or-not-to-foo}
                                 :agg4 {:gleep :glork}}})]
      (create-indexes :mock-key x :mock-old @x)
      (wait-for-updates)
      (wait-for-updates)
      (wait-for-updates)
      (wait-for-updates)
      (is (= #{{:foo :bar} #{{:foo :bar
                              :baz :bif}
                             {:foo :bar
                              :baz :boom}}
               {:foo :or-not-to-foo} #{{:foo :or-not-to-foo}}
               {:gleep :glork} #{{:gleep :glork}}
               {:baz :bif} #{{:foo :bar
                              :baz :bif}}
               {:baz :boom} #{{:foo :bar
                               :baz :boom}}}
             (:indexes @x))))))

;;   (testing "no new aggregates"
;;     (let [aggs [{:foo :bar}]]
;;       (is (= []
;;              (find-updated-aggregates aggs aggs)))))

;;   (testing "new aggregates"
;;     (let [old [{:foo :bar}]
;;           new [{:foo :bar}
;;                {:baz :bif}
;;                {:bam :boom}]]
;;       (is (= [{:baz :bif}
;;               {:bam :boom}]
;;              (find-updated-aggregates old new))))))

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
;;       (send sref assoc :aggregates {"john" {:username "john"}})
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

;; (run-tests)
