package com.sendclipper.model;

/**
 * Filters undelivered events.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ListWebhookDispatchesParams {
  private String state;
  private String endpointId;
  private String cursor;
  private Integer limit;

  private ListWebhookDispatchesParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * {@code dead_lettered} (the default), {@code retrying} (pending or delivering) or {@code all}.
   *
   * @return the state
   */
  public String getState() {
    return state;
  }

  public String getEndpointId() {
    return endpointId;
  }

  public String getCursor() {
    return cursor;
  }

  /**
   * 1-100.
   *
   * @return the limit
   */
  public Integer getLimit() {
    return limit;
  }

  /** Builds {@link ListWebhookDispatchesParams}. A builder builds once. */
  public static final class Builder {
    private final ListWebhookDispatchesParams p = new ListWebhookDispatchesParams();
    private boolean built;

    private Builder() {}

    /**
     * {@code dead_lettered} (the default), {@code retrying} (pending or delivering) or {@code all}.
     *
     * @param state the state
     * @return this builder
     */
    public Builder state(String state) {
      check();
      p.state = state;
      return this;
    }
    public Builder endpointId(String endpointId) {
      check();
      p.endpointId = endpointId;
      return this;
    }
    public Builder cursor(String cursor) {
      check();
      p.cursor = cursor;
      return this;
    }
    /**
     * 1-100.
     *
     * @param limit the limit
     * @return this builder
     */
    public Builder limit(Integer limit) {
      check();
      p.limit = limit;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public ListWebhookDispatchesParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ListWebhookDispatchesParams");
      }
    }
  }
}
