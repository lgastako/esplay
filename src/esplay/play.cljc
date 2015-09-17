(ns esplay.play
  (:require [clojure.set :as set]
            [esplay.core :as es]
            [its.log :as log]))

(def event-type first)
(def event-args second)

(def event-store (es/create-store))

(defn make-event-counting-projection [& [key]]
  (let [key (or key "esn:total-events")]
    (fn [sref event]
      (send sref
            (fn [ref]
              (let [current-events (or (get-in ref [:aggregates key]) 0)]
                (assoc-in ref :aggregates key (inc current-events))))))))

#_
(es/add-projection event-store
                   (fn [sref event]
                     (when (= :bank/user-created (event-type event))
                       (let [args (event-args event)
                             username (:username args)
                             aggregate-id (str "esn:user:" username)]
                         [[aggregate-id {:username username
                                         :created-at (:created-at args)}]]))))

(defn username-available? [sref username]
  (let [results (es/search sref :username username)
        result (not results)]
    (log/debug :username-available? {:username username
                                     :results results
                                     :result result})
    result))

(defn now []
  "fake timestamp")

(defn valid-username? [username]
  (and (string? username)
       (> (count username) 0)))

(defn create-user [sref {:keys [username]}]
  (let [event
        (cond
          (not (valid-username? username))            [:bank/user-creation-failed
                                                       {:username username
                                                        :reason :invalid-username}]
          (not (username-available? sref username))  [:bank/user-creation-failed
                                                       {:username username
                                                        :reason :taken}]
          :else                                       [:bank/user-created
                                                       {:username username
                                                        :created-at (now)}])]
    (log/debug :create-user {:sref sref})
    (es/post-event! sref event)))

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
