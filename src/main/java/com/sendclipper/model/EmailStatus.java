package com.sendclipper.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** A message's delivery state. */
public enum EmailStatus {
  QUEUED("queued"),
  SENT("sent"),
  DELIVERED("delivered"),
  OPENED("opened"),
  CLICKED("clicked"),
  BOUNCED("bounced"),
  COMPLAINED("complained"),
  FAILED("failed"),
  /** A value this version of the SDK does not know yet (the API only ever adds values). */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  EmailStatus(String value) {
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
