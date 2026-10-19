package com.sendclipper.model;

/** An endpoint with its full {@code whsec_} signing secret, shown once. */
public class WebhookEndpointWithSecret extends WebhookEndpoint {
  private String secret;

  /**
   * The signing secret: store it.
   *
   * @return the secret
   */
  public String getSecret() {
    return secret;
  }

}
