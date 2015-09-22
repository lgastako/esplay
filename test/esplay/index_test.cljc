(ns esplay.index-test
  (:require [clojure.test :refer :all]
            [esplay.index :as index]
            [esplay.store :as store]
            [its.log :as log]))

(deftest test-all-keys-from
  (testing "no xs"
    (is (= #{}
           (index/all-keys-from nil))))

  (testing "no keys"
    (is (= #{}
           (index/all-keys-from [{} {} {}]))))

  (testing "some keys"
    (is (= #{:foo :id}
           (index/all-keys-from [{:foo :bar} {:foo :bar :id 2}])))))

(deftest test-create-indexes
  (testing "basic index creation"
    (let [x (agent {:aggregates #{{:id 1
                                   :foo :bar
                                   :baz :bif}
                                  {:id 2
                                   :foo :bar
                                   :baz :boom}
                                  {:id 3
                                   :foo :or-not-to-foo}
                                  {:id 4
                                   :gleep :glork}}})]
      (index/create :mock-key x :mock-old @x)
      (await x)
      (log/warn :settled {:x x})
      (is (= {{:baz :bif} #{{:id 1
                             :foo :bar
                             :baz :bif}}
              {:baz :boom} #{{:id 2
                              :foo :bar
                              :baz :boom}},
              {:gleep :glork} #{{:id 4
                                 :gleep :glork}}
              {:foo :bar} #{{:id 1
                             :foo :bar
                             :baz :bif}
                            {:id 2
                             :foo :bar
                             :baz :boom}}
              {:foo :or-not-to-foo} #{{:id 3
                                       :foo :or-not-to-foo}}
              {:id 3} #{{:id 3
                         :foo :or-not-to-foo}}
              {:id 2} #{{:id 2
                         :foo :bar
                         :baz :boom}}
              {:id 4} #{{:id 4
                         :gleep :glork}}
              {:id 1} #{{:id 1
                         :foo :bar
                         :baz :bif}}}
             (:index @x))))))


(deftest test-all
  (testing "zero kvs"
    (is (= {} (index/all (store/create)))))

  (testing "non-zero kvs"
    (let [sref (store/create)
          aggs #{{:foo :bar}
                 {:bar :baz}}]
      (send sref assoc :aggregates aggs)
      (await sref)
      (is (= (:aggregates @sref)
             (index/all sref))))))

(deftest test-search
  (testing "zero kvs"
    ;; this means return everything, but it's an empty store, so zero results
    (is (nil? (index/search (store/create)))))

  (testing "odd number of kvs"
    (is (thrown? AssertionError (index/search (store/create) :foo))))

  (testing "a single kv with no results"
    (let [sref (store/create)
          aggs {}]
      (send sref assoc :aggregates aggs)
      (await sref)
      (is (= aggs (:aggregates @sref)))
      (is (= []
             (index/search sref
                           :username "john")))))

  (testing "a single kv with results"
    (let [sref (store/create)]
      (send sref assoc :aggregates #{:username "john"})
      (await sref)
      (index/create :mock-key sref :mock-old @sref)
      (await sref)
      (is (= #{:john {:username "john"}}
             (index/search sref
                     :username "john")))))


  )

(run-tests)


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
