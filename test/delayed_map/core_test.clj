(ns delayed-map.core-test
  (:import (java.io Writer StringWriter)
           (delayed_map DelayedMap))
  (:use clojure.test
        delayed-map.core))

; Helpers

(defmacro isnt
  ([form]
   `(is (not ~form)))
  ([form msg]
   `(is (not ~form) ~msg)))

(defn- test-map []
  (delayed-map
    {:foo 100}
    (fn [seed] {:bar (* 2 (:foo seed))})))

(defn- print-method-str [obj]
  (let [^Writer sw (StringWriter.)]
    (print-method obj sw)
    (str sw)))

(defn- seed-keys [m]
  (-> m .getData keys set))

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

(deftest seed-is-kept
  (let [m (delayed-map {:foo "old"}
                    (fn [x] {:foo "new"}))]
    (is (= "old" (:foo m)))
    (doall m)
    (is (realized? m))
    (is (= "old" (:foo m)))))

(deftest map-assoc
  (let [m (test-map)
        n (assoc m :lol 999)]
    (is (instance? DelayedMap n))
    (is (= #{:foo :lol}
           (seed-keys n)))
    (is (= #{:foo}
           (seed-keys m)) "Original map was mutated by `assoc`")
    (isnt (realized? m))))

(deftest map-dissoc
  (let [m (delayed-map {:foo 1}
                    (fn [_] {:foo 2}))
        n (dissoc m :foo)]
    (is (empty? (keys n)))
    (is (realized? m))
    (isnt (instance? DelayedMap n))))

(deftest map-ops-and-equality
  (let [plain {:foo 100 :bar 200}]
    (are [fun] (= (fun plain)
                  (fun (test-map)))
         keys
         vals
         seq
         count
         #(contains? % :foo)
         #(contains? % :bar)
         #(contains? % :lol))))

(deftest printing
  (let [m (test-map)]
    (is (= "{:foo 100, ...}"
           (.toString m)
           (print-method-str m)))
    (seq m)
    (is (= (print-method-str {:foo 100 :bar 200})
           (.toString m)
           (print-method-str m)))))
