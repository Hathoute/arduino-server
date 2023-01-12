package com.hathoute.n7.utils;
import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.Properties;

public final class ConfigManager {

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
    final var value = isNull(envValue) ? properties.getProperty(property) : envValue;
    if (isNull(value)) {
      throw new IllegalStateException("Property " + property + " not found");
    }

    return value;
  }

  private String toEnvProperty(final String property) {
    return property.toUpperCase().replace('.', '_');
  }
}
