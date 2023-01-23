package com.hathoute.n7.handler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.hathoute.n7.utils.InputStreamWrapper;
import com.hathoute.n7.utils.OutputStreamWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingPongHandler implements RequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(PingPongHandler.class);

  @Override
  public void handle(final InputStreamWrapper inputStream,
      final OutputStreamWrapper outputStream) {
    logger.trace("PingPongHandler.handle(inputStream, outputStream)");

    try {
      outputStream.writeString("OK", StandardCharsets.US_ASCII);
    } catch (final IOException e) {
      logger.warn("Error while handling request", e);
    }
  }
}
