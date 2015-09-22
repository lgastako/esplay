(ns esplay.event)

(defn post! [sref event]
  (send sref update-in [:events] conj event))



