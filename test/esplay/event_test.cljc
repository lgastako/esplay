(ns esplay.event-test
  (:require [clojure.test :refer :all]
            [esplay.event :as event]
            [esplay.store :as store]))

(deftest test-post-event!
  (testing "posting event to an empty store"
    (let [sref (store/create)]
      (event/post! sref {:event-type :foo})
      (await sref)
      (is (= [{:event-type :foo}]
             (:events @sref)))))

  (testing "events are posted in order"
    (letfn [(make-event [n]
              {:event-type :n
               :args {:n n}})]
      (let [sref (store/create)]
        (doseq [n (range 10)]
          (event/post! sref (make-event n))
          (await sref))
        (is (= (map make-event (range 10))
               (:events @sref)))))))

;; (run-tests)
