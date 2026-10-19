package com.sendclipper.model;

import java.util.List;

/**
 * Registers an endpoint.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class CreateWebhookParams {
  private String url;
  private List<WebhookEventType> events;
  private Boolean allowInsecureHttp;

  private CreateWebhookParams() {}

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

  /**
   * Accepts a plain {@code http://} URL. Off by default.
   *
   * @return the allow insecure http
   */
  public Boolean getAllowInsecureHttp() {
    return allowInsecureHttp;
  }

  /** Builds {@link CreateWebhookParams}. A builder builds once. */
  public static final class Builder {
    private final CreateWebhookParams p = new CreateWebhookParams();
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
    /**
     * Accepts a plain {@code http://} URL. Off by default.
     *
     * @param allowInsecureHttp the allow insecure http
     * @return this builder
     */
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
    public CreateWebhookParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its CreateWebhookParams");
      }
    }
  }
}
