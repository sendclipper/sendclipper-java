package com.sendclipper.model;

import com.fasterxml.jackson.databind.JsonNode;

/** Why one batch item was refused. */
public class BatchItemError extends ApiObject {
  private String code;
  private String message;
  private JsonNode detail;

  /**
   * The API's code, e.g. {@code template_variables_missing}.
   *
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * A sentence meant for a person.
   *
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Code-specific facts, or null.
   *
   * @return the detail
   */
  public JsonNode getDetail() {
    return detail;
  }

}
