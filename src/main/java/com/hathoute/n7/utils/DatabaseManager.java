package com.hathoute.n7.utils;
import com.hathoute.n7.model.AlertOperation;
import com.hathoute.n7.model.AlertOperationType;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import javax.sql.DataSource;

public final class DatabaseManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
  private static DatabaseManager instance;

  private final DataSource dataSource;
  private final String databaseName;

  private DatabaseManager(final DataSource dataSource, final String databaseName)
      throws SQLException {
    this.dataSource = dataSource;
    this.databaseName = databaseName;
    createDatabaseAndTables();
  }

  public static void initialize() throws PropertyVetoException, SQLException {
    LOGGER.debug("Initializing DatabaseManager");

    final var host = ConfigManager.getInstance().getString("database.host") +
                     ":" + ConfigManager.getInstance().getString("database.port");
    final var user = ConfigManager.getInstance().getString("database.user");
    final var password = ConfigManager.getInstance().getString("database.password");
    final var databaseName = ConfigManager.getInstance().getString("database.name");

    final var dataSource = new ComboPooledDataSource();
    dataSource.setDriverClass("org.mariadb.jdbc.Driver");
    dataSource.setJdbcUrl("jdbc:mariadb://%s/".formatted(host));
    dataSource.setUser(user);
    dataSource.setPassword(password);

    instance = new DatabaseManager(dataSource, databaseName);
    LOGGER.debug("Finished initializing DatabaseManager");
  }

  public static DatabaseManager getInstance() {
    if (instance == null) {
      throw new IllegalStateException("DatabaseManager not initialized");
    }
    return instance;
  }

  public void insertData(final int metricId, final float value) throws SQLException {
    final var insertQuery = "INSERT INTO %s.data (metric_id, value) VALUES (?, ?)"
        .formatted(databaseName);
    try (final var connection = dataSource.getConnection();
        final var preparedStatement = connection.prepareStatement(insertQuery)) {
      preparedStatement.setInt(1, metricId);
      preparedStatement.setFloat(2, value);
      preparedStatement.executeUpdate();
    }
  }

  public String getMetricName(final int metricId) throws SQLException {
    final var selectQuery = "SELECT name FROM %s.metrics WHERE id = ?".formatted(databaseName);
    try (final var connection = dataSource.getConnection();
        final var preparedStatement = connection.prepareStatement(selectQuery)) {
      preparedStatement.setInt(1, metricId);
      try (final var resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getString("name");
        }
      }
    }
    return null;
  }

  public Optional<Integer> getMetricId(final String gazId) throws SQLException {
    final var selectQuery = "SELECT id FROM %s.metrics WHERE gazId = ?".formatted(databaseName);
    try (final var connection = dataSource.getConnection();
        final var preparedStatement = connection.prepareStatement(selectQuery)) {
      preparedStatement.setString(1, gazId);
      try (final var resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(resultSet.getInt("id"));
        }
      }
    }
    return Optional.empty();
  }

  public Collection<AlertOperation> metricOperations(final int metricId) throws SQLException {
    final var operationsQuery = ("SELECT id, type, value FROM %s.alerts "
                                 + "WHERE metric_id = ?").formatted(databaseName);
    try (final var connection = dataSource.getConnection();
        final var preparedStatement = connection.prepareStatement(operationsQuery)) {
      preparedStatement.setInt(1, metricId);
      try (final var resultSet = preparedStatement.executeQuery()) {
        final var list = new ArrayList<AlertOperation>();
        while (resultSet.next()) {
          final var operation = AlertOperationType.valueOf(resultSet.getString("type"));
          final var value = resultSet.getFloat("value");
          list.add(new AlertOperation(metricId, operation, value));
        }
        return list;
      }
    }
  }

  private void createDatabaseAndTables() throws SQLException {
    final var dbQuery = "CREATE DATABASE IF NOT EXISTS %s".formatted(databaseName)
                        + " DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;";
    // Would be nice to use InnoDB engine to benefit from row-level locking, but
    // our use-case is so simple we're fine with MyISAM.
    final var tableMetricsQuery = "CREATE TABLE IF NOT EXISTS %s.metrics (".formatted(databaseName)
                                  + "id INT NOT NULL AUTO_INCREMENT,"
                                  + "name VARCHAR(255) NOT NULL,"
                                  + "gazId VARCHAR(4) NOT NULL UNIQUE,"
                                  + "PRIMARY KEY (id)"
                                  + ");";
    final var tableDataQuery = "CREATE TABLE IF NOT EXISTS %s.data (".formatted(databaseName)
                               + "id INT NOT NULL AUTO_INCREMENT,"
                               + "metric_id INT NOT NULL,"
                               + "value FLOAT NOT NULL,"
                               + "timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                               + "PRIMARY KEY (id),"
                               + "FOREIGN KEY (metric_id) REFERENCES metrics(id)"
                               + ");";
    final var tableAlertsQuery = "CREATE TABLE IF NOT EXISTS %s.alerts (".formatted(databaseName)
                                 + "id INT NOT NULL AUTO_INCREMENT,"
                                 + "metric_id INT NOT NULL,"
                                 + "value FLOAT NOT NULL,"
                                 + "type ENUM('LT', 'GT', 'EQ') NOT NULL,"
                                 + "PRIMARY KEY (id),"
                                 + "FOREIGN KEY (metric_id) REFERENCES metrics(id)"
                                 + ");";

    try (final var conn = dataSource.getConnection()) {
      try (final var stmt = conn.createStatement()) {
        stmt.execute(dbQuery);
      }
      conn.setCatalog(databaseName);
      try (final var stmt = conn.createStatement()) {
        stmt.execute(tableMetricsQuery);
      }
      try (final var stmt = conn.createStatement()) {
        stmt.execute(tableDataQuery);
      }
      try (final var stmt = conn.createStatement()) {
        stmt.execute(tableAlertsQuery);
      }
    }
  }

}
