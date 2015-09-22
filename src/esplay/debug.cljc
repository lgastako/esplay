(ns esplay.debug
  (:require [esplay.core :as es]
            [its.log :as log]))

(log/set-level! :debug)

(def store (es/create-store))

(es/add-projection store
                   (fn [sval event]
                     (when (= :foo (:event-type event))
                       (let [agg-id (str "a" (hash event))
                             agg-val event
                             count-id (str "c" (hash event))
                             new-count (-> sval
                                           (get-in [:aggregates count-id] 0)
                                           inc)]
                         [[agg-id agg-val]
                          [count-id new-count]]))))

(es/post-event! store {:event-type :foo
                       :args {:test 1}})
