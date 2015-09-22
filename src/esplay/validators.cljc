(ns esplay.validators
  (:require [esplay.schemas :refer [EventStore]]
            [schema.core :as s #?@(:cljs [:include-macros true])]))

(def sval (partial s/validate EventStore))
