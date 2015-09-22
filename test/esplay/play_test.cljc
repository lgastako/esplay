(ns esplay.play-test
  (:require [clojure.test :refer :all]
            [esplay.core :as es]
            [esplay.index :as index]
            [esplay.play :refer :all]
            [esplay.store :as store]
            [its.log :as log]))

(deftest test-valid-username?
  (testing "is valid"
    (is (valid-username? "john"))
    (is (valid-username? "a"))
    (is (valid-username? "a b c")))

  (testing "not valid"
    (is (not (valid-username? nil)))
    (is (not (valid-username? "")))
    (is (not (valid-username? 1)))
    (is (not (valid-username? [:a :b])))
    (is (not (valid-username? :foo)))))

(deftest test-create-user
  (testing "create user does not exist"
    (let [store (store/create)]
      (create-user store "john")
      (await store)
      (is (= #{"foo" "bar"}
             (:events @store)))
      (is (= #{"baz" "bif"}
             (:aggregates @store )))))

  (testing "create user does not exist"))

(deftest test-username-available?
  (testing "username is available"
    (let [store (store/create)]
      (is (username-available? store "john"))
      (is (username-available? store "jacob"))
      (is (username-available? store "jingleheimer schmidt"))))

  (testing "username is not available"
    (let [store (store/create)]
      (create-user store {:username "john2"})
      (create-user store {:username "jacob2"})
      (create-user store {:username "jingleheimer schmidt2"})
      (await store)
      (log/debug :store-after-3 {:store store})
      (is (not (username-available? store "john2")))
      (is (not (username-available? store "jacob2")))
      (is (not (username-available? store "jingleheimer schmidt2")))
      (is (username-available? store "other")))))

;; (run-tests)
