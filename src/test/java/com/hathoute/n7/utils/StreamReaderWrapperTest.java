package com.hathoute.n7.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
class StreamReaderWrapperTest {

  private static final String TEST_STRING = "test string";
  private static final float TEST_FLOAT = 1.875f;
  private static final byte[] TEST_FLOAT_BIG_ENDIAN = new byte[] {0x3f, (byte)0xf0, 0x00, 0x00};

  static {
    assert Arrays.equals(TEST_FLOAT_BIG_ENDIAN, float2ByteArray(TEST_FLOAT));
  }

  private InputStream getStringInputStreamReader() {
    return new ByteArrayInputStream(TEST_STRING.getBytes());
  }

  private InputStream getFloatInputStreamReader() {
    return new ByteArrayInputStream(TEST_FLOAT_BIG_ENDIAN);
  }

  @Test
  void should_read_string_when_enough_data_in_buffer() throws Exception {
    final var streamReaderWrapper = new StreamReaderWrapper(getStringInputStreamReader());

    final var result = streamReaderWrapper.readString(TEST_STRING.length(), true);

    assertEquals(TEST_STRING, result);
  }

  @Test
  void should_throw_exception_when_not_enough_data_for_string() {
    final var streamReaderWrapper = new StreamReaderWrapper(getStringInputStreamReader());
    var testLength = TEST_STRING.length() + 1;

    final var exception = assertThrows(
      IOException.class,
      () -> streamReaderWrapper.readString(testLength, true)
    );

    assertEquals(exception.getMessage(), "Expected " + testLength + " bytes, but got " + TEST_STRING.length());
  }

  @Test
  void should_read_string_when_not_fixed_length_and_not_enough_data_in_buffer() throws Exception {
    final var streamReaderWrapper = new StreamReaderWrapper(getStringInputStreamReader());

    final var result = streamReaderWrapper.readString(TEST_STRING.length(), false);

    assertEquals(TEST_STRING, result);
  }

  @Test
  void should_read_float() throws Exception {
    final var streamReaderWrapper = new StreamReaderWrapper(getFloatInputStreamReader());

    final var result = streamReaderWrapper.readFloat();

    assertEquals(TEST_FLOAT, result);
  }

  @Test
  void should_throw_exception_when_not_enough_data_for_float() throws IOException {
    var inputStream = getFloatInputStreamReader();
    // Read one byte so that we have only 3 bytes left in the buffer
    inputStream.read();
    final var streamReaderWrapper = new StreamReaderWrapper(inputStream);

    final var exception = assertThrows(
      IOException.class,
      streamReaderWrapper::readFloat
    );

    assertEquals(exception.getMessage(), "Expected 4 bytes, but got 3");
  }

  public static byte [] float2ByteArray (float value) {
    return ByteBuffer.allocate(4).putFloat(value).array();
  }
}