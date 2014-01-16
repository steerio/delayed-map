package delayed_map;

import clojure.lang.*;
import java.util.Iterator;

public class DelayedMap implements IPending, Iterable, Associative, IPersistentCollection, IPersistentMap, Seqable, ILookup {
  private static IFn merge = RT.var("clojure.core", "merge");
  private IPersistentMap data;
  private IFn loader;

  public DelayedMap(IPersistentMap seed, IFn loader) {
    data = seed;
    this.loader = loader;
  }

  private synchronized void load() {
    if (loader != null) {
      data = (IPersistentMap) merge.invoke(loader.invoke(data), data);
      loader = null;
    }
  }

  public IPersistentMap getData() {
    return data;
  }

  public String toString() {
    return RT.printString(this);
  }

  // IPending

  public boolean isRealized() {
    return loader == null;
  }

  // Iterable

  public Iterator iterator() {
    load();
    return data.iterator();
  }

  // Associative

  public boolean containsKey(Object key) {
    if (data.containsKey(key)) {
      return true;
    } else {
      load();
      return data.containsKey(key);
    }
  }

  public IMapEntry entryAt(Object key) {
    if (!data.containsKey(key)) load();
    return data.entryAt(key);
  }

  // IPersistentMap

  public IPersistentMap assoc(Object key, Object val) {
    IPersistentMap res = data.assoc(key, val);
    if (loader != null) return new DelayedMap(res, loader);
    return res;
  }

  public IPersistentMap assocEx(Object key, Object val) {
    IPersistentMap res = data.assocEx(key, val);
    if (loader != null) return new DelayedMap(res, loader);
    return res;
  }

  public IPersistentMap without(Object key) {
    load();
    return data.without(key);
  }

  // IPersistentCollection

  public int count() {
    load();
    return data.count();
  }

  public IPersistentCollection cons(Object o) {
    load();
    return data.cons(o);
  }

  public IPersistentCollection empty() {
    return data.empty();
  }

  public boolean equiv(Object o) {
    load();
    return data.equiv(o);
  }

  // Seqable

  public ISeq seq() {
    load();
    return data.seq();
  }

  // ILookup

  public Object valAt(Object key) {
    if (!data.containsKey(key)) load();
    return data.valAt(key);
  }

  public Object valAt(Object key, Object notFound) {
    if (!data.containsKey(key)) load();
    return data.valAt(key, notFound);
  }
}
