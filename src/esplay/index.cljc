(ns esplay.index
  (:require [clojure.set :refer [index]]
            [its.log :as log]
            [schema.core :as s #?@(:cljs [:include-macros true])]))

(defn all-keys-from [xs]
  (log/debug :all-keys-from {:xs xs})
  (->> xs
       ;; (map second)
       (map keys)
       (filter identity)
       flatten
       (into #{})))

(log/set-level! :warn)

(defn make-index [entities key]
  (-> entities
      (index [key])
      (dissoc {})))

(defn create [_ ref _ new]
  ;; For now we generate all indexes on every change.  This is insane and will
  ;; be optimized in the future.
  (let [entities (->> new :aggregates vals (into #{}))
        keys (all-keys-from entities)
        indexes (mapv (partial make-index entities) keys)]
    (log/warn :indexes indexes)
    ;; Even more insane we send them all off individually instead of once, but
    ;; remember, this is the fast-to-write code path.
    (send ref assoc :indexes indexes)))

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
  :under-construction)
