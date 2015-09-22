(ns esplay.play
  (:require [clojure.set :as set]
            [esplay.core :as es]
            [esplay.event :as event]
            [esplay.index :as index]
            [esplay.projection :as projection]
            [esplay.store :as store]
            [its.log :as log]))

(def event-type first)
(def event-args second)

(defn make-event-counting-projection [& [key]]
  (let [key (or key "esn:total-events")]
    (fn [sref event]
      (send sref
            (fn [ref]
              (let [current-events (or (get-in ref [:aggregates key]) 0)]
                (assoc-in ref :aggregates key (inc current-events))))))))

(defn create-play-store []
  (let [store (store/create)]
    (projection/add store
                    (fn [sval event]
                      (when (= :bank/user-created (event-type event))
                        (let [args (event-args event)
                              username (:username args)
                              aggregate-id (str "esn:user:" username)]
                          [[aggregate-id {:username username
                                          :created-at (:created-at args)}]]))))
    store))

(def event-store (create-play-store))

(defn username-available? [sref username]
  (empty? (index/search sref :username username)))

(defn now []
  "fake timestamp")

(defn valid-username? [username]
  (and (string? username)
       (> (count username) 0)))

(defn create-user [sref {:keys [username]}]
  (let [event
        (cond
          (not (valid-username? username))           {:event-type :bank/user-creation-failed
                                                      :args {:username username
                                                             :reason :invalid-username}}
          (not (username-available? sref username))  {:event-type :bank/user-creation-failed
                                                      :args {:username username
                                                             :reason :taken}}
          :else                                      {:event-type :bank/user-created
                                                      :args {:username username
                                                             :created-at (now)}})]
    (log/debug :create-user {:sref sref
                             :event event})
    (event/post! sref event)))

;; (defn open-account [store {:keys [username]}]
;;   ;; ...
;;   )

;; (register-command :create-user create-user)
;; (register-command :open-account open-account)

;; (execute-command! :create-user {:username "john"})
;; (execute-command! :open-account {:username "john"})

;; (post-event! [:bank/user-created {:username "john"
;;                                   :created-at ...}])

;; (post-event! [:bank/account-opened {:username "john"}])

;; (create-user event-store {:username "john"})
