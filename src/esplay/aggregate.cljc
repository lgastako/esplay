(ns esplay.aggregate
  (:refer-clojure :exclude [update]))

(defn add [sval [agg-id agg]]
  (update-in sval [:aggregates] assoc agg-id agg))

(defn update [sval agg-id f]
  (update-in sval [:aggregates agg-id] f))
