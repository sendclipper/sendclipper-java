package com.sendclipper.model;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * Tunes {@code sandboxes().messages().waitFor(...)}.
 *
 * <pre>{@code
 * WaitForOptions.builder()
 *     .match(m -> m.getSubject().equals("Welcome"))
 *     .timeout(Duration.ofSeconds(30))
 *     .build()
 * }</pre>
 */
public final class WaitForOptions {
  private Predicate<SandboxMessageSummary> match;
  private Duration interval = Duration.ofSeconds(1);
  private Duration timeout = Duration.ofSeconds(30);

  private WaitForOptions() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Picks the message to wait for.
   *
   * @return the predicate, or null for any message trapped after the call started
   */
  public Predicate<SandboxMessageSummary> getMatch() {
    return match;
  }

  /**
   * The time between polls. Default 1 second.
   *
   * @return the interval
   */
  public Duration getInterval() {
    return interval;
  }

  /**
   * How long to wait in all. Default 30 seconds.
   *
   * @return the timeout
   */
  public Duration getTimeout() {
    return timeout;
  }

  /** Builds {@link WaitForOptions}. */
  public static final class Builder {
    private final WaitForOptions o = new WaitForOptions();

    private Builder() {}

    /**
     * Picks the message to wait for.
     *
     * @param match accepts the wanted message
     * @return this builder
     */
    public Builder match(Predicate<SandboxMessageSummary> match) {
      o.match = match;
      return this;
    }

    /**
     * Sets the time between polls.
     *
     * @param interval a positive duration
     * @return this builder
     */
    public Builder interval(Duration interval) {
      o.interval = interval;
      return this;
    }

    /**
     * Sets how long to wait in all; past it the call throws {@code timeout}.
     *
     * @param timeout a positive duration
     * @return this builder
     */
    public Builder timeout(Duration timeout) {
      o.timeout = timeout;
      return this;
    }

    /**
     * Builds the options.
     *
     * @return a copy of what was set
     */
    public WaitForOptions build() {
      WaitForOptions copy = new WaitForOptions();
      copy.match = o.match;
      copy.interval = o.interval;
      copy.timeout = o.timeout;
      return copy;
    }
  }
}
