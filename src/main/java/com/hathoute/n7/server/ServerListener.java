package com.hathoute.n7.server;

import com.hathoute.n7.exception.HandlerNotFoundException;
import com.hathoute.n7.exception.HeaderMismatchException;
import com.hathoute.n7.handler.RequestHandlerFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerListener {
  private static final Logger logger = LoggerFactory.getLogger(ServerListener.class);
  private static final char[] HEADER = new char[]{'g', 'z', 's', 'h'};

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
    private final InputStreamReader inputStreamReader;
    private final OutputStreamWriter outputStreamWriter;

    public ThreadRunner(final Socket socket) throws IOException {
      this.socket = socket;
      inputStreamReader = new InputStreamReader(socket.getInputStream());
      outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
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
          inputStreamReader.close();
          outputStreamWriter.close();
          socket.close();
        } catch (final IOException e) {
          logger.warn("Could not close socket related resources", e);
        }
      }
    }

    private void checkHeader() throws HeaderMismatchException {
      final var header = new char[4];
      try {
        inputStreamReader.read(header, 0, 4);
        if (!Arrays.equals(header, HEADER)) {
          throw new HeaderMismatchException(
              "Expected " + Arrays.toString(HEADER) + " but got "
              + Arrays.toString(header));
        }
      } catch (final IOException e) {
        throw new HeaderMismatchException("Exception while reading input", e);
      }
    }

    private void executeHandler() throws HandlerNotFoundException {
      final int handlerId;
      try {
        handlerId = inputStreamReader.read();
      } catch (final IOException e) {
        throw new HandlerNotFoundException("Could not read handler id");
      }

      final var requestHandler = RequestHandlerFactory.createFromId(handlerId);
      requestHandler.handle(inputStreamReader, outputStreamWriter);
    }
  }
}
