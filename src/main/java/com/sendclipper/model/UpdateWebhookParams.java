package com.sendclipper.model;

import java.util.List;

/**
 * Changes the fields you set.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class UpdateWebhookParams {
  private String url;
  private List<WebhookEventType> events;
  private Boolean active;
  private Boolean allowInsecureHttp;

  private UpdateWebhookParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public String getUrl() {
    return url;
  }

  public List<WebhookEventType> getEvents() {
    return events;
  }

  public Boolean getActive() {
    return active;
  }

  public Boolean getAllowInsecureHttp() {
    return allowInsecureHttp;
  }

  /** Builds {@link UpdateWebhookParams}. A builder builds once. */
  public static final class Builder {
    private final UpdateWebhookParams p = new UpdateWebhookParams();
    private boolean built;

    private Builder() {}

    public Builder url(String url) {
      check();
      p.url = url;
      return this;
    }
    public Builder events(List<WebhookEventType> events) {
      check();
      p.events = events;
      return this;
    }
    public Builder active(Boolean active) {
      check();
      p.active = active;
      return this;
    }
    public Builder allowInsecureHttp(Boolean allowInsecureHttp) {
      check();
      p.allowInsecureHttp = allowInsecureHttp;
      return this;
    }

    /**
     * Sets the events to subscribe to.
     *
     * @param events the events
     * @return this builder
     */
    public Builder events(WebhookEventType... events) {
      return events(java.util.Arrays.asList(events));
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public UpdateWebhookParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its UpdateWebhookParams");
      }
    }
  }
}
