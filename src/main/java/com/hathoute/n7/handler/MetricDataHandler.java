package com.hathoute.n7.handler;
import com.hathoute.n7.processor.DataProcessorProvider;
import com.hathoute.n7.utils.Cache;
import com.hathoute.n7.utils.DatabaseManager;
import com.hathoute.n7.utils.StreamReaderWrapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricDataHandler implements RequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(MetricDataHandler.class);
  private static final Cache<String, Integer> METRIC_CACHE = Cache.fromSupplier(
      MetricDataHandler::loadMetricId);

  @Override
  public void handle(final InputStreamReader inputStreamReader,
      final OutputStreamWriter outputStreamWriter) {
    try {
      final var wrapper = new StreamReaderWrapper(inputStreamReader);
      final var gazId = wrapper.readString(3, true);
      final var value = wrapper.readFloat();

      final var processors = DataProcessorProvider.getProcessors();
      Optional.ofNullable(METRIC_CACHE.get(gazId)).ifPresentOrElse(
          metricId -> processors.forEach(processor -> processor.process(metricId, value)),
          () -> logger.warn("Metric gazId \"{}\" not found", gazId));
    } catch (final IOException e) {
      logger.warn("Error while handling request", e);
    }
  }

  private static Integer loadMetricId(final String gazId) {
    try {
      return DatabaseManager.getInstance().getMetricId(gazId).orElse(null);
    } catch (final SQLException e) {
      logger.error("Error while loading metricId for gazId: {}", gazId, e);
      // Cache loader produces null on error so that no value will be saved.
      return null;
    }
  }
}
