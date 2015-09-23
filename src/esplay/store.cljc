(ns esplay.store
  (:require [esplay.errors :as errors]
            [esplay.index :as index]
            [esplay.schemas :refer [initial-event-store]]
            [esplay.validators :as validators]
            [esplay.projection :as projection]
            [its.log :as log]))

(defn create []
  (let [sref (agent initial-event-store
                    :validator validators/sval
                    :error-handler errors/handle)]
    (add-watch sref :projector projection/apply-all)
    (add-watch sref :indexer index/create)
    sref))
