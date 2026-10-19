package com.sendclipper.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

/** One step of a message's timeline. */
public class EmailEvent extends ApiObject {
  private String id;
  private String type;
  private JsonNode meta;
  private Instant createdAt;

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public JsonNode getMeta() {
    return meta;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

}
