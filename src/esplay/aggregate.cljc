(ns esplay.aggregate
  (:refer-clojure :exclude [update]))

(defn add [sval [agg-id agg]]
  (update-in sval [:aggregates] assoc agg-id agg))

(defn by-id [sval agg-id]
  (get-in sval [:aggregates agg-id]))

(defn update [sval agg-id f]
  (update-in sval [:aggregates agg-id] f))
