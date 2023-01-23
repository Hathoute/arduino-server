package com.hathoute.n7.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutputStreamWrapperTest {


  @Test
  void should_write_byte() throws Exception {
    final var outputStream = new ByteArrayOutputStream();
    final var wrapper = new OutputStreamWrapper(outputStream);
    final var testByte = (byte) 0x01;

    wrapper.writeByte(testByte);

    assertEquals(1, outputStream.size());
    assertEquals(testByte, outputStream.toByteArray()[0]);
  }

  @Test
  void should_write_string() throws Exception {
    final var outputStream = new ByteArrayOutputStream();
    final var wrapper = new OutputStreamWrapper(outputStream);
    final var testString = "test string";

    wrapper.writeString(testString, StandardCharsets.US_ASCII);

    assertEquals(testString.length(), outputStream.size());
    assertEquals(testString, outputStream.toString());
  }

  @Test
  void should_not_lose_data_when_writing_string() throws Exception {
    final var outputStream = new ByteArrayOutputStream();
    final var wrapper = new OutputStreamWrapper(outputStream);
    final var testString = "test";
    final var testString2 = " string";

    wrapper.writeString(testString, StandardCharsets.US_ASCII);
    wrapper.writeString(testString2, StandardCharsets.US_ASCII);

    assertEquals(testString.length() + testString2.length(), outputStream.size());
    assertEquals(testString + testString2, outputStream.toString());
  }

  @Test
  void should_close_output_stream() throws Exception {
    final var outputStream = mock(OutputStream.class);
    final var wrapper = new OutputStreamWrapper(outputStream);

    wrapper.close();

    verify(outputStream).close();
  }
}