package com.hathoute.n7.handler;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public interface RequestHandler {
  void handle(final InputStreamReader inputStreamReader,
      final OutputStreamWriter outputStreamWriter);
}
