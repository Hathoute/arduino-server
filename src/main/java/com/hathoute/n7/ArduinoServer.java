package com.hathoute.n7;
import com.hathoute.n7.server.ServerListener;
import com.hathoute.n7.utils.ConfigManager;
import com.hathoute.n7.utils.DatabaseManager;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArduinoServer {
  private static final Logger logger = LoggerFactory.getLogger(ArduinoServer.class);

  public static void main(final String[] args) throws Exception {
    ConfigManager.initialize();
    DatabaseManager.initialize();
    startServer();
  }

  private static void startServer() {
    final var port = ConfigManager.getInstance().getInt("server.port");
    logger.info("Attempting to start server on port {}", port);
    try {
      ServerListener.start(port);
    } catch (final IOException e) {
      logger.error("Could not start server", e);
    }
  }
}