(ns esplay.projection
  (:require [clojure.data :refer [diff]]
            [clojure.set :refer [index intersection]]
            [esplay.indexes :as indexes]
            [esplay.schema :refer :all]
            [its.log :as log]
            [schema.core :as s #?@(:cljs [:include-macros true])]))

(defn find-new-events [as bs]
  (let [na (count as)
        nb (count bs)]
    (assert (>= nb na))
    (assert (= as (subvec bs 0 na)))
    (subvec bs na nb)))

(defn project-sval [sval projection event]
  (let [aggregates (projection sval event)]
    (reduce add-agg sval aggregates)))

(defn project [sref projection event]
  (log/debug :project {:sref sref
                       :projection projection
                       :event event})
  (send sref project-sval projection event))

(defn apply-projections [_ sref old new]
  (log/debug :apply-projections)
  (let [old-events (:events old)
        new-events (:events new)
        projections (:projections new)]
    (when-not (= old-events new-events)
      (let [added-events (find-new-events old-events new-events)]
        (log/debug :found-events (count added-events))
        (doseq [event added-events]
          (doseq [projection projections]
            (project sref projection event)))))))
