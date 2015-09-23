(defproject esplay "0.1.0-SNAPSHOT"
  :description "Me playing around with event sourcing / CQRS"
  :url "http://github.com/lgastako/esplay"
  ;; :auto {:default {:file-pattern #"\.(clj|cljs|cljx|cljc|edn)$"}}
  :source-paths ["src"]
  :test-paths ["test"]
  :dependencies [[its-log "0.2.2" :exclusions [org.clojure/clojure org.clojure/core.async]]
                 [org.clojure/clojure "1.7.0"]
                 [prismatic/schema "0.4.3"]
                 [org.clojure/test.check "0.8.2"]])
