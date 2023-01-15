package com.hathoute.n7;
import com.hathoute.n7.server.ServerListener;
import com.hathoute.n7.utils.ConfigManager;
import com.hathoute.n7.utils.DatabaseManager;
import java.io.IOException;

import com.hathoute.n7.utils.MailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArduinoServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ArduinoServer.class);

  public static void main(final String[] args) throws Exception {
    LOGGER.info("Starting ArduinoServer");

    ConfigManager.initialize();
    DatabaseManager.initialize();
    MailManager.initialize();
    startServer();
  }

  private static void startServer() {
    final var port = ConfigManager.getInstance().getInt("server.port");
    LOGGER.info("Attempting to start server on port {}", port);
    try {
      ServerListener.start(port);
    } catch (final IOException e) {
      LOGGER.error("Could not start server", e);
    }
  }
}