package com.hathoute.n7.server;

import com.hathoute.n7.exception.HandlerNotFoundException;
import com.hathoute.n7.exception.HeaderMismatchException;
import com.hathoute.n7.handler.RequestHandlerFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.hathoute.n7.utils.InputStreamWrapper;
import com.hathoute.n7.utils.OutputStreamWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerListener.class);
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
      LOGGER.info("Server started on {}:{}", serverSocket.getInetAddress(), port);
      while (true) {
        final var socket = serverSocket.accept();
        LOGGER.trace("Accepted connection from {}:{}", socket.getInetAddress(), socket.getPort());

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
      LOGGER.warn("Cannot start thread to handle request", e);
      return;
    }

    final var thread = new Thread(threadRunner);
    // Threads run concurrently, identify them for logging purposes.
    thread.setName(socket.getInetAddress() + ":" + socket.getPort());
    thread.start();
  }

  static class ThreadRunner implements Runnable {
    private final Socket socket;
    private final InputStreamWrapper inputStream;
    private final OutputStreamWrapper outputStream;

    public ThreadRunner(final Socket socket) throws IOException {
      this.socket = socket;
      inputStream = new InputStreamWrapper(socket.getInputStream());
      outputStream = new OutputStreamWrapper(socket.getOutputStream());
    }

    @Override
    public void run() {
      LOGGER.trace("ThreadRunner.run()");

      try {
        checkHeader();
        executeHandler();
      } catch (final HeaderMismatchException hme) {
        LOGGER.warn("Header mismatch for socket request", hme);
      } catch (final HandlerNotFoundException hnfe) {
        LOGGER.warn("Handler not found for socket request", hnfe);
      } finally {
        try {
          socket.close();
        } catch (final IOException e) {
          LOGGER.warn("Could not close socket", e);
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
      requestHandler.handle(inputStream, outputStream);
    }
  }
}
