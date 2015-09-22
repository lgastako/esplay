(ns esplay.schemas-tests
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [esplay.schemas :as schemas :refer :all]
            [schema.core :as s #?@(:cljs [:include-macros true])]))

;; (defspec event-type-is-key-or-string 100
;;   (prop/for-all [s gen/string]
;;                 (s/validate EventType s)))

;; (defn event-type? [x]
;;   (or (keyword? x)
;;       (string? x)))

;; (def non-event-type? (comp not event-type?))

;; (defspec event-type-is-not-anything-else 100
;;   (prop/for-all [x (gen/such-that non-event-type? gen/any)]
;;                 (not (s/validate EventType x))))


;; (run-tests)
