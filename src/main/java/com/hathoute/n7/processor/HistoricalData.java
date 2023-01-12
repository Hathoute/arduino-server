package com.hathoute.n7.processor;
import com.hathoute.n7.utils.DatabaseManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoricalData implements DataProcessor {
  private static final Logger logger = LoggerFactory.getLogger(HistoricalData.class);

  @Override
  public void process(final int metricId, final float value) {
    try {
      DatabaseManager.getInstance().insertData(metricId, value);
    } catch (final SQLException e) {
      logger.error("Error while inserting data (metricId: {}, value:  {}", metricId, value, e);
    }
  }

}
