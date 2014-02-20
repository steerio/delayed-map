# delayed-map

A small Clojure library that implements a two-stage map.

## Installation

Include the following dependency in your `project.clj` file:

```clojure
:dependencies [[delayed-map "0.1.0-SNAPSHOT"]]
```

## Usage

A delayed map is defined by a "seed" map and a loader function that returns
another map, the rest of the key-value pairs. This function receives the seed
as its sole argument.

```clojure
(use 'delayed-map.core)

(def lm
  (delayed-map {:foo 1 :bar 2}
            (fn [seed] {:baz (inc (:bar seed))})))
```

The loader function will not be called until necessary.

## Realization

Realization is triggered transparently by one of the following:

* A read on a key that is not part of the seed.
* Any operation that uses the seqable or collection abstractions (`count`, `seq` and so on).
* Certain map ops like `merge` or `dissoc` (see [notes](#notes) below).

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

Operations that won't realize a delayed map include:

* `assoc`, as it always returns another delayed map with a modified seed for
  not yet realized delayed maps (it returns a regular map for realized ones).
* `select-keys`, _if_ all the selected keys are present in the seed.

## Does it act like an immutable map?

Yes. To maintain Clojure's immutability guarantee on maps, the seed is _merged
into_ the result of the loader function, not the other way around.

```clojure
user=> (seq (delayed-map {:foo "old"} (fn [_] {:foo "new" :bar "hi"})))
([:foo "old"] [:bar "hi"])
```

## Any practical uses for this?

I came up with the idea while coding [Pundit](https://github.com/steerio/pundit),
a client library for the [Parse](https://parse.com/) platform.

The [Parse API](https://www.parse.com/docs/rest) returns so-called
[pointers](https://www.parse.com/docs/rest#objects-types) when a value in a
field is another database object. A pointer is nothing more than a map with the
model class name and the object ID; if you need the full object, you have to
load it based on this information.

This process can be made transparent using delayed maps for pointers, delaying
the actual request until it becomes necessary.

```clojure
(defn- load-pointer [{:keys [class-name object-id]}]
  (retrieve class-name object-id))

(defn- ptr->obj [ptr]
  (delayed-map
    (select-keys ptr [:class-name :object-id])
    load-pointer))
```

The decision to prevent `print-method` from realizing the map comes also from
this use. Circular references in the database would make a REPL keep on loading
objects forever otherwise (that is, until the heap is blown by the output
buffer).

## Notes

The `dissoc` operation realizes the map even if the key to be removed is in the
seed. The reason for this is that we cannot allow something like the following
to happen:

```clojure
user=> (def lm (delayed-map {:foo 1 :bar 2} (fn [_] {:bar 3})))
#'user/lm
user=> (:bar (dissoc lm :bar))
3
```

## License

Copyright © 2014 Roland Venesz / Wopata SARL

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
