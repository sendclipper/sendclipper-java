package com.sendclipper;

import java.net.http.HttpHeaders;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Adjusts one call. Pass it as the last argument of any method:
 *
 * <pre>{@code
 * client.emails().send(params, RequestOptions.builder()
 *     .idempotencyKey("order-" + order.getId() + "-receipt")
 *     .build());
 * }</pre>
 */
public final class RequestOptions {
  private static final RequestOptions NONE = builder().build();

  private final String idempotencyKey;
  private final Duration timeout;
  private final Integer maxRetries;
  private final Map<String, String> headers;
  private final Consumer<HttpHeaders> onResponseHeaders;

  private RequestOptions(Builder b) {
    this.idempotencyKey = b.idempotencyKey;
    this.timeout = b.timeout;
    this.maxRetries = b.maxRetries;
    this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(b.headers));
    this.onResponseHeaders = b.onResponseHeaders;
  }

  /**
   * No adjustments.
   *
   * @return the empty options
   */
  public static RequestOptions none() {
    return NONE;
  }

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Your own Idempotency-Key, or null.
   *
   * @return the key
   */
  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  /**
   * The per-attempt timeout for this call, or null for the client's.
   *
   * @return the timeout
   */
  public Duration getTimeout() {
    return timeout;
  }

  /**
   * The retry count for this call, or null for the client's.
   *
   * @return the count
   */
  public Integer getMaxRetries() {
    return maxRetries;
  }

  /**
   * Extra request headers.
   *
   * @return the headers, never null
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * Called with the response headers when the call succeeds, or null.
   *
   * @return the callback
   */
  public Consumer<HttpHeaders> getOnResponseHeaders() {
    return onResponseHeaders;
  }

  /** Builds {@link RequestOptions}. */
  public static final class Builder {
    private String idempotencyKey;
    private Duration timeout;
    private Integer maxRetries;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private Consumer<HttpHeaders> onResponseHeaders;

    private Builder() {}

    /**
     * Sets your own Idempotency-Key, so that YOUR retries of the same logical operation (a job
     * that runs again after a crash) are safe too. Without it every write still gets a fresh key
     * of its own.
     *
     * @param key a key that names the logical operation
     * @return this builder
     */
    public Builder idempotencyKey(String key) {
      this.idempotencyKey = key;
      return this;
    }

    /**
     * Overrides the client's per-attempt timeout for this call.
     *
     * @param timeout the timeout
     * @return this builder
     */
    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    /**
     * Overrides the client's retry count for this call.
     *
     * @param maxRetries how often a network error, 429 or 5xx is retried
     * @return this builder
     */
    public Builder maxRetries(int maxRetries) {
      this.maxRetries = Math.max(0, maxRetries);
      return this;
    }

    /**
     * Adds a header to this call.
     *
     * @param name the header name
     * @param value its value
     * @return this builder
     */
    public Builder header(String name, String value) {
      this.headers.put(name, value);
      return this;
    }

    /**
     * Receives the response headers when the call succeeds (on failure they are on the {@link
     * SendClipperException}).
     *
     * @param callback called once, with the headers
     * @return this builder
     */
    public Builder onResponseHeaders(Consumer<HttpHeaders> callback) {
      this.onResponseHeaders = callback;
      return this;
    }

    /**
     * Builds the options.
     *
     * @return the options
     */
    public RequestOptions build() {
      return new RequestOptions(this);
    }
  }
}
