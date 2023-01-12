package com.hathoute.n7.exception;
public class HeaderMismatchException extends Exception {
  public HeaderMismatchException(final String message) {
    super(message);
  }

  public HeaderMismatchException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
