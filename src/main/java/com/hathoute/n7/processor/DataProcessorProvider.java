package com.hathoute.n7.processor;
import java.util.Collection;
import java.util.List;

public class DataProcessorProvider {

  public static Collection<DataProcessor> getProcessors() {
    return List.of(new HistoricalData(), new Notification());
  }

}
