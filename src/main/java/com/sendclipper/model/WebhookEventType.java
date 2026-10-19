package com.sendclipper.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** An event a webhook can subscribe to. */
public enum WebhookEventType {
  EMAIL_DELIVERED("email.delivered"),
  EMAIL_OPENED("email.opened"),
  EMAIL_CLICKED("email.clicked"),
  EMAIL_BOUNCED("email.bounced"),
  EMAIL_COMPLAINED("email.complained"),
  EMAIL_FAILED("email.failed"),
  /** A value this version of the SDK does not know yet (the API only ever adds values). */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  WebhookEventType(String value) {
    this.value = value;
  }

  /**
   * The value as the API writes it.
   *
   * @return the wire value
   */
  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
