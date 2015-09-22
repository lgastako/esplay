(ns esplay.aggregate)

(defn add [sval [agg-id agg]]
  (update-in sval [:aggregates] conj agg))
