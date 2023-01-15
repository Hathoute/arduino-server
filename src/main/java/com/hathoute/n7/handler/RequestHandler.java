package com.hathoute.n7.handler;
import com.hathoute.n7.utils.StreamReaderWrapper;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public interface RequestHandler {
  void handle(final StreamReaderWrapper inputStreamReader,
      final OutputStreamWriter outputStreamWriter);
}
