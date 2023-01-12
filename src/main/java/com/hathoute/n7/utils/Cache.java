package com.hathoute.n7.utils;
import java.util.Map;
import java.util.function.Function;

public final class Cache<K, V> {

  private final Map<K, V> cacheValues;
  private final Function<K, V> loader;

  private Cache(final Function<K, V> loader) {
    this.loader = loader;
    cacheValues = new java.util.HashMap<>();
  }

  public static <K, V> Cache<K, V> fromSupplier(final Function<K, V> cacheLoader) {
    return new Cache<>(cacheLoader);
  }

  public V get(final K key) {
    synchronized (cacheValues) {
      return cacheValues.computeIfAbsent(key, loader);
    }
  }

  public void invalidate() {
    synchronized (cacheValues) {
      cacheValues.clear();
    }
  }
}
