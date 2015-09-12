(ns esplay.core
  (:require [clojure.data :refer [diff]]
            [clojure.set :refer [index
                                 intersection]]
            [its.log :as log]
            [schema.core :as s #?@(:cljs [:include-macros true])]))

(log/set-level! :debug)

(def EventType
  (s/either s/Keyword
            s/Str))

(def EventArgs
  {s/Any s/Any})

(def Event
  {:event-type EventType
   (s/optional-key :args) EventArgs})

(def Projection s/Any)

(def AggregateId s/Any)

(def Aggregate s/Any)

(def IndexKey s/Keyword)

(def Index #{s/Any})

(def EventStore
  {:events [Event]
   :projections [Projection]
   :aggregates {AggregateId Aggregate}
   :indices {IndexKey Index}})

(def initial-event-store {:events []
                          :projections []
                          :aggregates {}})

(defn find-new-events [as bs]
  (let [na (count as)
        nb (count bs)]
    (assert (>= nb na))
    (assert (= as (subvec bs 0 na)))
    (subvec bs na nb)))

(defn add-agg [ref [agg-id agg]]
  (update-in ref [:aggregates] assoc agg-id agg))

(defn project-ref [ref projection event]
  (let [aggregates (projection ref event)]
    (reduce add-agg ref aggregates)))

(defn project [store projection event]
  (log/debug :project {:store store
                       :projection projection
                       :event event})
  (swap! store project-ref projection event))

(defn apply-projections [_ ref old new]
  (log/debug :apply-projections)
  (let [old-events (:events old)
        new-events (:events new)
        projections (:projections new)]
    (when-not (= old-events new-events)
      (let [added-events (find-new-events old-events new-events)]
        (log/debug :found-events (count added-events))
        (doseq [event added-events]
          (doseq [projection projections]
            (project ref projection event)))))))

(defn find-updated-aggregates [old new]
  (-> old
      (diff new)
      second))

(defn all-keys-from [xs]
  (log/debug :all-keys-from {:xs xs})
  (->> xs
       (map keys)
       flatten
       (into #{})))

(defn index-key [ref key]
  (let [idx (index (:aggregates ref) [key])]
    (swap! ref assoc-in [:indexes key] idx)))

(defn update-indices [_ ref old new]
  (let [old-aggregates (:aggregates old)
        new-aggregates (:aggregates new)
        updated-aggregates (find-updated-aggregates old-aggregates new-aggregates)]
    ;; TODO: more efficient in the future
    (let [keys (all-keys-from updated-aggregates)]
      (doseq [key keys]
        (index-key ref key)))))

(defn create-store []
  (let [events (atom initial-event-store)]
    (add-watch events :projector apply-projections)
    (add-watch events :indexer update-indices)
    events))

(defn post-event! [store event]
  (swap! store update-in [:events] conj event))

(defn add-projection [store projection]
  (swap! store update-in [:projections] conj projection))

(defn search-index [index])

(defn search [store & kvs]
  (log/debug :search {:store store
                      :kvs kvs})
  (let [nkvs (count kvs)]
    (assert (even? nkvs))
    (assert (> nkvs 0)))
  (let [f (partial search-index (:indices store))]
    (->> [(vec kvs)]
         (into {})
         (map f)
         intersection)))
