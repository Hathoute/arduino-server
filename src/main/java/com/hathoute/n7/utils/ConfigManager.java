package com.hathoute.n7.utils;
import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

  private static ConfigManager instance;

  private final Properties properties;

  private ConfigManager(final Properties properties) {
    this.properties = properties;
  }

  public static void initialize() throws IOException {
    final var loader = Thread.currentThread().getContextClassLoader();
    final var properties = new Properties();
    properties.load(loader.getResourceAsStream("config.properties"));

    instance = new ConfigManager(properties);
  }

  public static ConfigManager getInstance() {
    if (instance == null) {
      throw new IllegalStateException("ConfigManager not initialized");
    }
    return instance;
  }

  public String getString(final String property) {
    final var envValue = System.getenv(toEnvProperty(property));
    if (!isNull(envValue)) {
      LOGGER.trace("Using environment variable {} for property {}", envValue, property);
      return envValue;
    }

    final var value = properties.getProperty(property);
    if (isNull(value)) {
      LOGGER.trace("Property {} not found", property);
      throw new IllegalStateException("Property " + property + " not found");
    }

    LOGGER.trace("Property {} = {}", property, value);
    return value;
  }

  public int getInt(final String property) {
    return Integer.parseInt(getString(property));
  }

  private String toEnvProperty(final String property) {
    return property.toUpperCase().replace('.', '_');
  }
}
