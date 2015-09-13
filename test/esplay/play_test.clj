(ns esplay.play-test
  (:require [clojure.test :refer :all]
            [esplay.core :as es]
            [esplay.play :refer :all]))

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

(deftest test-username-available?
  (testing "username is available"
    (let [store (es/create-store)]
      (is (username-available? store "john"))
      (is (username-available? store "jacob"))
      (is (username-available? store "jingleheimer schmidt"))))

  (testing "username is not available"
    (let [store (es/create-store)]
      (create-user store {:username "john2"})
      (create-user store {:username "jacob2"})
      (create-user store {:username "jingleheimer schmidt2"})
      (is (not (username-available? store "john2")))
      (is (not (username-available? store "jacob2")))
      (is (not (username-available? store "jingleheimer schmidt2")))
      (is (username-available? store "other")))))
