(ns lazy-map.core
  (:import (lazy_map LazyMap)))

(defmethod print-method LazyMap [m w]
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

(definline lazy-map [seed fun]
  `(LazyMap. ~seed ~fun))
