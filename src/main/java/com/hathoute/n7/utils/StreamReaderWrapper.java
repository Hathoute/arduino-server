package com.hathoute.n7.utils;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StreamReaderWrapper implements Closeable {
  private final InputStream inputStream;

  public StreamReaderWrapper(final InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public byte readByte() throws IOException {
    return (byte) inputStream.read();
  }

  public String readString(final int length, final boolean fixedLen) throws IOException {
    final var buffer = new char[length];
    final var reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
    final var readChars = reader.read(buffer, 0, length);
    if (fixedLen && readChars != length) {
      throw new IOException("Expected " + length + " bytes, but got " + readChars);
    }
    return new String(buffer);
  }

  public float readFloat() throws IOException {
    final var buffer = new byte[4];
    final var readBytes = inputStream.read(buffer, 0, 4);
    if (readBytes != 4) {
      throw new IOException("Expected 4 bytes, but got " + readBytes);
    }

    // Big endian...
    return ByteBuffer.wrap(buffer).getFloat();
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
  }
}
