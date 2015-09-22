(ns esplay.index
  (:require [clojure.set :refer [index intersection]]
            [its.log :as log]))

(defn all-keys-from [xs]
  (log/debug :all-keys-from {:xs xs})
  (->> xs
       (map keys)
       (filter identity)
       flatten
       (into #{})))

(defn make-index [entities key]
  (-> entities
      (index [key])
      (dissoc {})))

(defn merge-indexes [indexes]
  (reduce merge {} indexes))

(defn create [_ ref _ new]
  ;; For now we generate all indexes on every change.  This is insane and will
  ;; be optimized in the future.
  (let [entities (:aggregates new)
        keys (all-keys-from entities)
        indexes (mapv (partial make-index entities) keys)
        index (merge-indexes indexes)        ]
    ;; Even more insane we send them all off individually instead of once, but
    ;; remember, this is the fast-to-write code path.
    (when (not= (:index new)
                index)
      (send ref assoc :index index))))

(defn all [sref]
  (:aggregates @sref))

(defn all-by-kv [sval k v]
  (log/debug :all-by-kv {:k k
                         :v v
                         :sval sval})
  (let [locator {k v}
        index (:index sval)]
    (log/debug :looking-up {:locator locator})
    (when-let [results (get index locator)]
      (apply intersection results))))

(defn by-id [sval k v]
  (first (all-by-kv sval k v)))

(defn search [sref & kvs]
  (let [sval @sref
        nkvs (count kvs)]
    (log/debug :search {:sval sval
                        :kvs kvs})
    (assert (even? nkvs))
    (when (pos? nkvs)
      (log/debug :applying :all-by-kv)
      (->> kvs
           (partition 2)
           (mapv (partial apply all-by-kv sval))
           (filterv identity)
;;           (apply intersection)
           ))))
