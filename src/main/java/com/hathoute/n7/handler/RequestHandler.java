package com.hathoute.n7.handler;
import com.hathoute.n7.utils.InputStreamWrapper;
import com.hathoute.n7.utils.OutputStreamWrapper;

public interface RequestHandler {
  void handle(final InputStreamWrapper inputStream,
      final OutputStreamWrapper outputStream);
}
