package com.hathoute.n7.handler;
import com.hathoute.n7.exception.HandlerNotFoundException;

public final class RequestHandlerFactory {

  private RequestHandlerFactory() {
  }

  public static RequestHandler createFromId(final int identifier) throws HandlerNotFoundException {
    return switch (identifier) {
      case 0 -> new PingPongHandler();
      case 1 -> new MetricDataHandler();
      default ->
          throw new HandlerNotFoundException("No handler found for identifier: " + identifier);
    };
  }
}
