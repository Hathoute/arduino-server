package com.hathoute.n7.processor;
import java.util.Collection;
import java.util.List;

public class DataProcessorProvider {
  private static DataProcessorProvider instance;

  public static DataProcessorProvider getInstance() {
    if (instance == null) {
      instance = new DataProcessorProvider();
    }
    return instance;
  }

  private final List<DataProcessor> dataProcessors;

  private DataProcessorProvider() {
    // Create unmodifiable list of processors
    dataProcessors = List.of(new HistoricalData(), new Notification());
  }

  public Collection<DataProcessor> getProcessors() {
    return dataProcessors;
  }
}
