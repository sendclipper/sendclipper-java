package com.sendclipper.model;

/** An {@code {id, name}} pair. */
public class NamedRef extends ApiObject {
  private String id;
  private String name;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

}
