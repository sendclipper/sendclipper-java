package com.sendclipper.model;

import com.sendclipper.core.Json;

/**
 * The base of every response model.
 *
 * <p>The API only ever ADDS fields inside /v1; it never removes or retypes one. Fields a response
 * carries that a model does not list are ignored.
 */
public abstract class ApiObject {
  /** For subclasses. */
  protected ApiObject() {}

  /** The object as JSON, for logs and debugging. */
  @Override
  public String toString() {
    return getClass().getSimpleName() + Json.writeString(this);
  }
}
