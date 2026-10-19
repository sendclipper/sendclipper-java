package com.sendclipper;

/** Why {@link Webhook#verify} refused a delivery. Answer such a request with 400. */
public class WebhookVerificationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final String reason;

  /**
   * Makes the exception.
   *
   * @param reason which check failed
   * @param message a sentence meant for a person
   */
  public WebhookVerificationException(String reason, String message) {
    super(message);
    this.reason = reason;
  }

  /**
   * Which check failed: {@code missing_header}, {@code malformed_header}, {@code
   * timestamp_out_of_tolerance}, {@code signature_mismatch} or {@code invalid_payload}.
   *
   * @return the reason
   */
  public String getReason() {
    return reason;
  }

  @Override
  public String toString() {
    return "WebhookVerificationException: " + reason + ": " + getMessage();
  }
}
