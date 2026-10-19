package com.sendclipper.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** Whether a message is {@code transactional} (the default) or {@code marketing}. */
public enum SendClass {
  /** A receipt, a password reset: no unsubscribe header, no footer. */
  TRANSACTIONAL("transactional"),
  /** Adds one-click unsubscribe and the postal-address footer. */
  MARKETING("marketing"),
  /** A value this version of the SDK does not know yet (the API only ever adds values). */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  SendClass(String value) {
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
