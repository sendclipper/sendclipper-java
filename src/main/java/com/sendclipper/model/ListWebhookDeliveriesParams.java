package com.sendclipper.model;

/**
 * Filters delivery attempts.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ListWebhookDeliveriesParams {
  private String endpointId;
  private Boolean finalOnly;
  private Integer limit;

  private ListWebhookDeliveriesParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public String getEndpointId() {
    return endpointId;
  }

  public Boolean getFinalOnly() {
    return finalOnly;
  }

  public Integer getLimit() {
    return limit;
  }

  /** Builds {@link ListWebhookDeliveriesParams}. A builder builds once. */
  public static final class Builder {
    private final ListWebhookDeliveriesParams p = new ListWebhookDeliveriesParams();
    private boolean built;

    private Builder() {}

    public Builder endpointId(String endpointId) {
      check();
      p.endpointId = endpointId;
      return this;
    }
    public Builder finalOnly(Boolean finalOnly) {
      check();
      p.finalOnly = finalOnly;
      return this;
    }
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
    public ListWebhookDeliveriesParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ListWebhookDeliveriesParams");
      }
    }
  }
}
