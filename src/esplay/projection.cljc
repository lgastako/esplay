(ns esplay.projection
  (:require [esplay.aggregate :as aggregate]
            [its.log :as log]))

(defn find-new-events [as bs]
  (let [na (count as)
        nb (count bs)]
    (assert (>= nb na))
    (assert (= as (subvec bs 0 na)))
    (subvec bs na nb)))

(defn project-sval [sval projection event]
  (let [aggregates (projection sval event)]
    (reduce aggregate/add sval aggregates)))

(defn project [sref projection event]
  (log/debug :project {:sref sref
                       :projection projection
                       :event event})
  (send sref project-sval projection event))

(defn apply-all [_ sref old new]
  (log/debug :projection/apply-all)
  (let [old-events (:events old)
        new-events (:events new)
        projections (:projections new)]
    (when-not (= old-events new-events)
      (let [added-events (find-new-events old-events new-events)]
        (log/debug :found-events (count added-events))
        (doseq [event added-events]
          (doseq [projection projections]
            (project sref projection event)))))))

(defn add [sref projection]
  (send sref update-in [:projections] conj projection))
