package com.hathoute.n7.utils;
import java.io.IOException;
import java.io.InputStreamReader;

public class StreamReaderWrapper {
  private final InputStreamReader inputStreamReader;

  public StreamReaderWrapper(final InputStreamReader inputStreamReader) {
    this.inputStreamReader = inputStreamReader;
  }

  public String readString(final int length, final boolean fixedLen) throws IOException {
    final var buffer = new char[length];
    final var readBytes = inputStreamReader.read(buffer, 0, length);
    if (fixedLen && readBytes != length) {
      throw new IOException("Expected " + length + " bytes, but got " + readBytes);
    }
    return new String(buffer);
  }

  public float readFloat() throws IOException {
    // Big endian...
    final var data = readString(4, true).getBytes();
    return Float.intBitsToFloat(
        (data[0] & 0xff) << 24 | (data[1] & 0xff) << 16 | (data[2] & 0xff) << 8 | (data[3] & 0xff));
  }
}
