(ns esplay.store
  (:require [esplay.index :as index]
            [esplay.schemas :refer [initial-event-store]]
            [esplay.validators :as validators]
            [esplay.projection :as projection]
            [its.log :as log]))

(defn handle-errors [sref ex]
  (log/error :store/handle-errors {:sref sref :ex ex}))

(defn create []
  (let [sref (agent initial-event-store
                    :validator validators/sval
                    :error-handler handle-errors)]
    (add-watch sref :projector projection/apply-all)
    (add-watch sref :indexer index/create)
    sref))
