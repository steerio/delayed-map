package lazy_map;

import clojure.lang.*;
import java.util.Iterator;

public class LazyMap implements IPending, Iterable, Associative, IPersistentCollection, IPersistentMap, Seqable, ILookup {
  private static IFn merge = RT.var("clojure.core", "merge");
  private IPersistentMap data;
  private IFn loader;
  private boolean pending;

  public LazyMap(IPersistentMap seed, IFn loader) {
    data = seed;
    this.loader = loader;
    pending = true;
  }

  private void load() {
    if (pending) {
      data = (IPersistentMap) merge.invoke(loader.invoke(data), data);
      pending = false;
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
    return !pending;
  }

  // Iterable

  public Iterator iterator() {
    load();
    return data.iterator();
  }

  // Associative

  public boolean containsKey(Object key) {
    return data.containsKey(key);
  }

  public IMapEntry entryAt(Object key) {
    if (!data.containsKey(key)) load();
    return data.entryAt(key);
  }

  // IPersistentMap

  public IPersistentMap assoc(Object key, Object val) {
    load();
    return data.assoc(key, val);
  }

  public IPersistentMap assocEx(Object key, Object val) {
    load();
    return data.assocEx(key, val);
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
