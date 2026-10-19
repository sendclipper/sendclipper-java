package com.sendclipper.model;

/** created is false when the address was already suppressed; row is the entry either way. */
public class SuppressionCreated extends ApiObject {
  private boolean created;
  private Suppression row;

  public boolean isCreated() {
    return created;
  }

  public Suppression getRow() {
    return row;
  }

}
