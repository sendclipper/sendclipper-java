package com.sendclipper.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/** Where Amazon SES registers a domain. */
public enum Region {
  US_EAST_1("us-east-1"),
  EU_WEST_1("eu-west-1"),
  US_WEST_2("us-west-2"),
  /** A value this version of the SDK does not know yet (the API only ever adds values). */
  @JsonEnumDefaultValue
  UNKNOWN("unknown");

  private final String value;

  Region(String value) {
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
