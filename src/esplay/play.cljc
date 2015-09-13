(ns esplay.play
  (:require [clojure.set :as set]
            [esplay.core :as es]
            [its.log :as log]))

(def event-type first)
(def event-args second)

(def event-store (es/create-store))

(defn make-event-counting-projection [& [key]]
  (let [key (or key "esn:total-events")]
    (fn [store event]
      (swap! store
             (fn [ref]
               (let [current-events (or (get-in ref [:aggregates key]) 0)]
                 (assoc-in ref :aggregates key (inc current-events))))))))

#_
(es/add-projection event-store
                   (fn [store event]
                     (when (= :bank/user-created (event-type event))
                       (let [args (event-args event)
                             username (:username args)
                             aggregate-id (str "esn:user:" username)]
                         [[aggregate-id {:username username
                                         :created-at (:created-at args)}]]))))

(defn username-available? [store username]
  (log/debug :username-available? {:check (es/search store :username username)})
  (not (es/search store :username username)))

(defn now []
  "fake timestamp")

(defn valid-username? [username]
  (and (string? username)
       (> (count username) 0)))

(defn create-user [store {:keys [username]}]
  (let [event
        (cond
          (not (valid-username? username))            [:bank/user-creation-failed
                                                       {:username username
                                                        :reason :invalid-username}]
          (not (username-available? store username))  [:bank/user-creation-failed
                                                       {:username username
                                                        :reason :taken}]
          :else                                       [:bank/user-created
                                                       {:username username
                                                        :created-at (now)}])]
    (log/debug :create-user {:store store})
    (es/post-event! store event)))

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
