package com.sendclipper.model;

/**
 * Scopes a bulk replay.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ReplayAllParams {
  private String state;
  private String endpointId;

  private ReplayAllParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public String getState() {
    return state;
  }

  public String getEndpointId() {
    return endpointId;
  }

  /** Builds {@link ReplayAllParams}. A builder builds once. */
  public static final class Builder {
    private final ReplayAllParams p = new ReplayAllParams();
    private boolean built;

    private Builder() {}

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

    /**
     * Builds the params.
     *
     * @return the params
     */
    public ReplayAllParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ReplayAllParams");
      }
    }
  }
}
