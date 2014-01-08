(ns lazy-map.core-test
  (:import (java.io Writer StringWriter))
  (:use clojure.test
        lazy-map.core))

; Helpers

(defmacro isnt
  ([form]
   `(is (not ~form)))
  ([form msg]
   `(is (not ~form) ~msg)))

(defn- test-map []
  (lazy-map
    {:foo 100}
    (fn [seed] {:bar (* 2 (:foo seed))})))

(defn- print-method-str [obj]
  (let [^Writer sw (StringWriter.)]
    (print-method obj sw)
    (str sw)))

; Tests

(deftest key-in-seed
  (let [m (test-map)]
    (isnt (realized? m))
    (:foo m)
    (isnt (realized? m))))

(deftest realization
  (let [m (test-map)]
    (is (= 200 (:bar m)))
    (is (realized? m))))

(deftest seed-kept
  (let [m (lazy-map {:foo "old"}
                    (fn [x] {:foo "new" :bar 123}))]
    (is (= "old" (:foo m)))
    (:bar m)
    (is (= "old" (:foo m)))))

(deftest map-ops
  (let [plain {:foo 100 :bar 200}]
    (are [fun] (= (fun plain)
                  (fun (test-map)))
         keys
         vals
         seq
         count
         #(contains? % :foo)
         #(contains? % :bar)
         #(contains? % :lol)
         #(assoc % :lol 999)
         #(dissoc % :foo))))

(deftest printing
  (let [m (test-map)]
    (is (= "{:foo 100, ...}"
           (.toString m)
           (print-method-str m)))
    (seq m)
    (is (= (print-method-str {:foo 100 :bar 200})
           (.toString m)
           (print-method-str m)))))
