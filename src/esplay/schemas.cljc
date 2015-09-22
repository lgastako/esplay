(ns esplay.schemas
  (:require [schema.core :as s #?@(:cljs [:include-macros true])]))

(def EventType
  (s/either s/Keyword
            s/Str))

(def EventArgs
  {s/Any s/Any})

(def Event
  {:event-type EventType
   (s/optional-key :args) EventArgs})

(def Projection s/Any)

(def Aggregate s/Any)

(def Field s/Keyword)
(def FieldValue s/Any)

(def IndexEntry {Field FieldValue})

(def Index {IndexEntry #{Aggregate}})

(def EventStore
  {:aggregates #{Aggregate}
   :events [Event]
   :index Index
   :projections [Projection]})

(def initial-event-store
  {:aggregates {}
   :events []
   :index {}
   :projections []})
