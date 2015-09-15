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
   :indexes []
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
  (swap! sref project-sval projection event))

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
  (let [idx (-> sval
                :aggregates
                (index [key])
                (dissoc {}))]
    (assoc-in sval [:indexes key] idx)))

(defn update-indexes [_ sref old new]
  ;; This is not necessarily thread safe, but then again doesn't this whole
  ;; notion of receiving a reference that may have changed along with old/new
  ;; values create this situation in the first place?  Anyway, for now I'm
  ;; letting it be.
  (let [old-aggregates (:aggregates old)
        new-aggregates (:aggregates new)
        updated-aggregates (find-updated-aggregates old-aggregates new-aggregates)]
    ;; TODO: more efficient in the future
    (let [keys (all-keys-from updated-aggregates)
          new-indexes (reduce index-key new keys)]
      ;; This is where we get unsafe
      (swap! sref (fn [sval]
                    (loop [sval sval
                           indexes (:indexes new-indexes)]
                      (if-let [pair (first indexes)]
                        (do
                          (println :pair pair)
                          (flush)
                          (let [[key index] pair]
                            (println :key key :index index)
#_                            (recur sval (rest indexes))))
                        sval)))))))

(defn create-store []
  (let [sref (atom initial-event-store)]
    (add-watch sref :projector apply-projections)
    (add-watch sref :indexer update-indexes)
    sref))

(defn post-event! [sref event]
  (swap! sref update-in [:events] conj event))

(defn add-projection [sref projection]
  (swap! sref update-in [:projections] conj projection))

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
