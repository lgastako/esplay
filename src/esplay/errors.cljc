(ns esplay.errors
  (:require [its.log :as log]))

(defn handle [sref ex]
  (log/error :errors/handle {:sref sref :ex ex}))
