package com.hathoute.n7.processor;
import com.hathoute.n7.model.AlertOperation;
import com.hathoute.n7.utils.Cache;
import com.hathoute.n7.utils.DatabaseManager;
import java.sql.SQLException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notification implements DataProcessor {
  private static final Logger logger = LoggerFactory.getLogger(Notification.class);

  private final Cache<Integer, Collection<AlertOperation>> operationsCache;

  public Notification() {
    operationsCache = Cache.fromSupplier(this::loadOperations);
  }

  @Override
  public void process(final int metricId, final float value) {
    operationsCache.get(metricId).stream()
        .filter(operation -> operation.isTriggered(value))
        .forEach(operation -> notifyAlert(operation, value));
  }

  private void notifyAlert(final AlertOperation operation, final float value) {
    logger.info("Alert triggered (metricId: {}, value: {}, type: {}, threshold: {})",
        operation.metricId(), value, operation.type(), operation.value());
  }

  private Collection<AlertOperation> loadOperations(final int metricId) {
    try {
      return DatabaseManager.getInstance().metricOperations(metricId);
    } catch (final SQLException e) {
      logger.error("Error while loading operations for metricId: {}", metricId, e);
      // Cache loader produces null on error so that no value will be saved.
      return null;
    }
  }

}
