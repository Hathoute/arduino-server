package com.hathoute.n7.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class InputStreamWrapper implements Closeable {
  private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamWrapper.class);

  private final InputStream inputStream;

  public InputStreamWrapper(final InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public byte readByte() throws IOException {
    final var b = (byte) inputStream.read();
    LOGGER.trace("InputStreamWrapper.readByte() -> {}", b);
    return b;
  }

  public String readString(final int length, final boolean fixedLen) throws IOException {
    final var buffer = new byte[length];
    final var readChars = inputStream.read(buffer);
    if (fixedLen && readChars != length) {
      throw new IOException("Expected " + length + " bytes, but got " + readChars);
    }
    final var string = new String(buffer, StandardCharsets.US_ASCII);
    LOGGER.trace("InputStreamWrapper.readString({}, {}) -> {}", length, fixedLen, string);
    return string;
  }

  public float readFloat() throws IOException {
    final var buffer = new byte[4];
    final var readBytes = inputStream.read(buffer, 0, 4);
    if (readBytes != 4) {
      throw new IOException("Expected 4 bytes, but got " + readBytes);
    }

    // Big endian...
    final var f = ByteBuffer.wrap(buffer).getFloat();
    LOGGER.trace("InputStreamWrapper.readFloat() -> {}", f);
    return f;
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
  }
}
