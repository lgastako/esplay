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
  (send sref project-sval projection event))

(defn apply-all [_ sref old new]
  (let [old-events (:events old)
        new-events (:events new)
        projections (:projections new)]
    (log/error :apply-all {:old-events old-events
                           :new-events new-events
                           :same (= old-events new-events)})
    (when-not (= old-events new-events)
      (let [added-events (find-new-events old-events new-events)]
        (doseq [event added-events]
          (doseq [projection projections]
            (project sref projection event)))))))

(defn add [sref projection]
  (send sref update-in [:projections] conj projection))
