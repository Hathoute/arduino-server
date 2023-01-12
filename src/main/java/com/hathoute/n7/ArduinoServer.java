package com.hathoute.n7;
import com.hathoute.n7.utils.ConfigManager;
import com.hathoute.n7.utils.DatabaseManager;

public class ArduinoServer {
  public static void main(final String[] args) throws Exception {
    ConfigManager.initialize();
    DatabaseManager.initialize();
  }
}