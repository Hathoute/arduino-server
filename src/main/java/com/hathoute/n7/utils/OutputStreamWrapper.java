package com.hathoute.n7.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class OutputStreamWrapper implements Closeable {
  private static final Logger LOGGER = LoggerFactory.getLogger(OutputStreamWrapper.class);

  private final OutputStream outputStream;

  public OutputStreamWrapper(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public void writeByte(final byte b) throws IOException {
    LOGGER.trace("OutputStreamWrapper.writeByte({})", b);

    outputStream.write(b);
  }

  public void writeString(final String s, Charset charset) throws IOException {
    LOGGER.trace("OutputStreamWrapper.writeString({}, {})", s, charset);

    var bytes = s.getBytes(charset);
    outputStream.write(bytes);
  }

  @Override
  public void close() throws IOException {
    LOGGER.trace("OutputStreamWrapper.close()");

    outputStream.close();
  }
}
