package com.hathoute.n7.server;

import com.hathoute.n7.exception.HandlerNotFoundException;
import com.hathoute.n7.exception.HeaderMismatchException;
import com.hathoute.n7.handler.RequestHandlerFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.hathoute.n7.utils.StreamReaderWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerListener {
  private static final Logger logger = LoggerFactory.getLogger(ServerListener.class);
  private static final String HEADER = "gzsh";

  private final int port;

  private ServerListener(final int port) throws IOException {
    this.port = port;
    startListening();
  }

  public static ServerListener start(final int port) throws IOException {
    return new ServerListener(port);
  }

  private void startListening() throws IOException {
    try (final var serverSocket = new ServerSocket(port)) {
      while (true) {
        final var socket = serverSocket.accept();
        // Socket is not thread safe, but here it is manipulated by a single thread, so it's ok.
        startThread(socket);
      }
    }
  }

  private void startThread(final Socket socket) {
    final ThreadRunner threadRunner;
    try {
      threadRunner = new ThreadRunner(socket);
    } catch (final IOException e) {
      logger.warn("Cannot start thread to handle request", e);
      return;
    }

    final var thread = new Thread(threadRunner);
    thread.start();
  }

  static class ThreadRunner implements Runnable {
    private final Socket socket;
    private final StreamReaderWrapper inputStream;
    private final OutputStreamWriter outputStreamWriter;

    public ThreadRunner(final Socket socket) throws IOException {
      this.socket = socket;
      inputStream = new StreamReaderWrapper(socket.getInputStream());
      outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII);
    }

    @Override
    public void run() {
      try {
        checkHeader();
        executeHandler();
      } catch (final HeaderMismatchException hme) {
        logger.warn("Header mismatch for socket request", hme);
      } catch (final HandlerNotFoundException hnfe) {
        logger.warn("Handler not found for socket request", hnfe);
      } finally {
        try {
          inputStream.close();
          outputStreamWriter.close();
          socket.close();
        } catch (final IOException e) {
          logger.warn("Could not close socket related resources", e);
        }
      }
    }

    private void checkHeader() throws HeaderMismatchException {
      try {
        var header = inputStream.readString(4, true);
        if (!HEADER.equals(header)) {
          throw new HeaderMismatchException(
              "Expected " + HEADER + " but got " + header + " instead");
        }
      } catch (final IOException e) {
        throw new HeaderMismatchException("Exception while reading input", e);
      }
    }

    private void executeHandler() throws HandlerNotFoundException {
      final int handlerId;
      try {
        handlerId = inputStream.readByte();
      } catch (final IOException e) {
        throw new HandlerNotFoundException("Could not read handler id");
      }

      final var requestHandler = RequestHandlerFactory.createFromId(handlerId);
      requestHandler.handle(inputStream, outputStreamWriter);
    }
  }
}
