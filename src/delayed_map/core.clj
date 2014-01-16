(ns delayed-map.core
  (:import (delayed_map DelayedMap)))

(defmethod print-method DelayedMap [m w]
  (let [data (.getData m)]
    (if (realized? m)
      (print-method data w)
      (do
        (.write w "{")
        (doseq [[k v] data]
          (print-method k w)
          (.write w " ")
          (print-method v w)
          (.write w ", "))
        (.write w "...}")))))

(definline delayed-map
  "Creates a delayed map with the given seed map and loader function."
  [seed loader]
  `(DelayedMap. ~seed ~loader))
