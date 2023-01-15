package com.hathoute.n7.handler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.hathoute.n7.utils.StreamReaderWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingPongHandler implements RequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(PingPongHandler.class);

  @Override
  public void handle(final StreamReaderWrapper inputStreamReader,
      final OutputStreamWriter outputStreamWriter) {
    try {
      outputStreamWriter.write("OK");
    } catch (final IOException e) {
      logger.warn("Error while handling request", e);
    }
  }
}
