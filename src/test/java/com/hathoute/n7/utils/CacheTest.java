package com.hathoute.n7.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheTest {

  @Test
  void should_return_value_from_loader_when_not_cached() {
    final Function<String, String> loader = s -> "test " + s;
    final var cache = Cache.fromSupplier(loader);
    final var testKey = "key";

    final var value = cache.get(testKey);

    assertEquals(loader.apply(testKey), value);
  }

  @Test
  void should_use_cached_value_when_cached() {
    final var loader = (Function<String, String>) mock(Function.class);
    final var cache = Cache.fromSupplier(loader);
    final var testKey = "key";
    // Mock the behaviour of a database for example (different reference is returned for each call).
    when(loader.apply(testKey)).then(i -> "test " + i.getArgument(0));

    final var value1 = cache.get(testKey);
    final var value2 = cache.get(testKey);

    verify(loader, times(1)).apply(testKey);
    assertSame(value1, value2);
  }

  @Test
  void should_not_cache_null_values() {
    final var loader = (Function<String, String>) mock(Function.class);
    final var cache = Cache.fromSupplier(loader);
    final var testKey = "key";
    when(loader.apply(testKey)).thenReturn(null);

    var value1 = cache.get(testKey);
    var value2 = cache.get(testKey);

    verify(loader, times(2)).apply(testKey);
    assertNull(value1);
    assertNull(value2);
  }

  private static Stream<RuntimeException> provideExceptionsForTest() {
    return Stream.of(
      new RuntimeException("Test exception"),
      new NullPointerException("value is null"),
      new IllegalArgumentException("value is empty"),
      new IllegalStateException("value is invalid")
    );
  }

  @ParameterizedTest
  @MethodSource("provideExceptionsForTest")
  <T extends RuntimeException> void should_propagate_exception_from_loader(T exception) {
    final Function<String, String> loader = s -> {
      throw exception;
    };
    final var cache = Cache.fromSupplier(loader);
    final var testKey = "key";

    assertThrows(
      exception.getClass(),
      () -> cache.get(testKey),
      exception.getMessage()
    );
  }

  @Test
  void should_call_loader_after_invalidating() {
    final var loader = (Function<String, String>) mock(Function.class);
    final var cache = Cache.fromSupplier(loader);
    final var testKey = "key";
    // Mock the behaviour of a database for example (different reference is returned for each call).
    when(loader.apply(testKey)).then(i -> "test " + i.getArgument(0));
    final var value1 = cache.get(testKey);

    cache.invalidate();
    final var value2 = cache.get(testKey);

    verify(loader, times(2)).apply(testKey);
    assertNotSame(value1, value2);
  }

}