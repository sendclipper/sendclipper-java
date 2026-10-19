package com.sendclipper.core;

import java.time.Duration;

/** Reaches package-private helpers from tests in other packages. */
public final class TestAccess {
  private TestAccess() {}

  public static Duration backoff(int attempt) {
    return HttpCore.backoff(attempt);
  }
}
