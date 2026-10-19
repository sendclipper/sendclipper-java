package com.sendclipper.model;

/** A list of contacts. */
public class Audience extends ApiObject {
  private String id;
  private String name;
  private String color;
  private int count;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getColor() {
    return color;
  }

  public int getCount() {
    return count;
  }

}
