# lazy-map

A small Clojure library that implements a two-stage map.

## Usage

A lazy map is defined by a "seed" map and a loader function that returns
another map, the rest of the key-value pairs. This function receives the seed
as its sole argument.

```clojure
(use 'lazy-map.core)

(def lm
  (lazy-map {:foo 1 :bar 2}
            (fn [seed] {:baz (inc (:bar seed))})))
```

The loader function will not be called until necessary. It is triggered
transparently by one of the following:

* A read on a key that is not part of the seed
* Any operation that uses the seqable or collection abstractions (`count`, `seq`, etc.)
* Map ops like `assoc`, `dissoc`, `merge`, etc. This might change in the future -
  for example, `assoc` could return another lazy map with a different seed, and
  so on.

```clojure
user=> lm
{:foo 1, :bar 2, ...}
user=> (:foo lm)
1
user=> (realized? lm)
false
user=> lm
{:foo 1, :bar 2, ...}
user=> (seq lm)
([:bar 2] [:foo 1] [:baz 3])
user=> lm
{:bar 2, :foo 1, :baz 3}
user=> (realized? lm)
true
```

## Does it act like an immutable map?

Yes. To maintain Clojure's immutability guarantee on maps, the seed is _merged
into_ the result of the loader function, not the other way around.

```clojure
user=> (seq (lazy-map {:foo "old"} (fn [_] {:foo "new" :bar "hi"})))
([:foo "old"] [:bar "hi"])
```

## Any practical uses for this?

I came up with the idea while coding a new, soon to be released client library
for the [Parse](http://parse.com) platform.

The [Parse API](https://www.parse.com/docs/rest) returns so-called
[pointers](https://www.parse.com/docs/rest#objects-types) when a value in a
field is another database object. A pointer is nothing more than a map with the
model class name and the object ID; if you need the full object, you have to
load it based on this information.

This process can be made transparent using lazy maps for pointers, delaying the
actual request until it becomes necessary.

```clojure
(defn- load-pointer [{:keys [class-name object-id]}]
  (retrieve class-name object-id))

(defn- ptr->obj [ptr]
  (lazy-map
    (select-keys ptr [:class-name :object-id])
    load-pointer))
```

The decision to prevent `print-method` from realizing the map comes also from
this use. Circular references in the database would make a REPL keep on loading
objects forever otherwise (that is, until the heap is blown by the output
buffer).

## License

Copyright © 2014 Roland Venesz / Wopata SARL

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
