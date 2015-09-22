(ns esplay.core
  (:require [clojure.data :refer [diff]]
            [clojure.set :refer [index intersection]]
            [esplay.indexes :as indexes]
            [esplay.schemas :refer :all]
            [its.log :as log]
            [schema.core :as s #?@(:cljs [:include-macros true])]))

(defn handle-errors [sref ex]
  (log/error :store/handle-errors {:sref sref :ex ex}))

(defn create []
  (let [sref (agent initial-event-store
                    :validator validate-sval
                    :error-handler handle-store-errors)]
    (add-watch sref :projector apply-projections)
    (add-watch sref :indexer indexes/create)
    sref))
