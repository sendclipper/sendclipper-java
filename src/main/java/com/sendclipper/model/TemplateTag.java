package com.sendclipper.model;

/** Labels templates. */
public class TemplateTag extends ApiObject {
  private String id;
  private String name;
  private String color;
  private Integer templateCount;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getColor() {
    return color;
  }

  /**
   * How many templates carry it, or null.
   *
   * @return the template count
   */
  public Integer getTemplateCount() {
    return templateCount;
  }

}
