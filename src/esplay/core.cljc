(ns esplay.core
  (:require [clojure.data :refer [diff]]
            [clojure.set :refer [index intersection]]
            [its.log :as log]
            [schema.core :as s #?@(:cljs [:include-macros true])]))

;;(log/set-level! :debug)
(log/set-level! :off)

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
  {:aggregates {AggregateId Aggregate}
   :events [Event]
   :indexes {IndexKey Index}
   :projections [Projection]})

(def initial-event-store
  {:aggregates {}
   :events []
   :indexes {}
   :projections []})

(defn find-new-events [as bs]
  (let [na (count as)
        nb (count bs)]
    (assert (>= nb na))
    (assert (= as (subvec bs 0 na)))
    (subvec bs na nb)))

(defn add-agg [sval [agg-id agg]]
  (update-in sval [:aggregates] assoc agg-id agg))

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

(defn find-updated-aggregates [old new]
  (-> old
      (diff new)
      second
      (->> (filter identity))
      vec))

(defn all-keys-from [xs]
  (log/debug :all-keys-from {:xs xs})
  (->> xs
       (map keys)
       (filter identity)
       flatten
       (into #{})))

(defn index-key [sval key]
  (log/warn :index-key 1 sval)
  (log/warn :index-key 2 (-> sval
                             :aggregates))
  (log/warn :index-key 3 (-> sval
                             :aggregates
                             vals))
  (log/warn :index-key 4 (-> sval
                             :aggregates
                             vals
                             vec))
  (log/warn :index-key 5 (-> sval
                             :aggregates
                             vals
                             vec
                             (index [key])))
  (log/warn :index-key 5 (-> sval
                             :aggregates
                             vals
                             vec
                             (index [key])
                             (dissoc {})))
  (let [idx (-> sval
                :aggregates
                vals
                vec
                (index [key])
                (dissoc {}))]
    (let [res (assoc-in sval [:indexes key] idx)]
      (log/warn :index-key {:sval sval
                            :key key
                            :idx idx
                            :agg (:aggregates sval)
                            :res res})
      res)))

(log/set-level! :warn)

(defn set-index [sval key index]
  (assoc-in sval [:indexes key] index))

(defn update-indexes [_ sref old new]
  (let [old-aggregates (:aggregates old)
        new-aggregates (:aggregates new)
        updated-aggregates (find-updated-aggregates old-aggregates new-aggregates)]
    (when-not (empty? updated-aggregates)
      ;; TODO: more efficient in the future
      (let [keys (all-keys-from updated-aggregates)
            new-indexes (reduce index-key new keys)]
        (send sref (fn [sval]
                     (loop [sval sval
                            indexes (:indexes new-indexes)]
                       (if-let [pair (first indexes)]
                         (let [[key index] pair]
                           (recur (set-index sval key index)
                                  (rest indexes)))
                         sval))))))))

(def validate-sval (partial s/validate EventStore))

(defn handle-store-errors [sref ex]
  (log/error :handle-store-errors {:sref sref :ex ex}))

(defn create-store []
  (let [sref (agent initial-event-store
                    :validator validate-sval
                    :error-handler handle-store-errors)]
    (add-watch sref :projector apply-projections)
    (add-watch sref :indexer update-indexes)
    sref))

(defn post-event! [sref event]
  (send sref update-in [:events] conj event))

(defn add-projection [sref projection]
  (send sref update-in [:projections] conj projection))

(defn search-index [indexes query]
  (log/debug :search-index {:indexes indexes
                            :query query})
  nil)

(defn search [sref & kvs]
  (log/debug :search {:sref sref
                      :kvs kvs})
  (let [nkvs (count kvs)]
    (assert (even? nkvs))
    (assert (> nkvs 0)))
  ;; (let [indexes (:indexes @sref)
  ;;       f (partial search-index indexes)]
  ;;   (log/debug :vec-kvs1 [(vec kvs)])
  ;;   (log/debug :vec-kvs2 (into {} [(vec kvs)]))
  ;;   (log/debug :vec-kvs2 (map f (into {} [(vec kvs)])))
  ;;   ;;   (->> [(vec kvs)]
  ;;   ;;        (apply into {})
  ;;   ;;        (map f)
  ;;   ;;        intersection)
  ;;   )
  nil)
