package com.sendclipper.model;

/** One DNS record to publish. */
public class DnsRecord extends ApiObject {
  private String type;
  private String purpose;
  private String host;
  private String value;
  private String copyValue;
  private Integer priority;
  private String status;

  public String getType() {
    return type;
  }

  public String getPurpose() {
    return purpose;
  }

  public String getHost() {
    return host;
  }

  public String getValue() {
    return value;
  }

  public String getCopyValue() {
    return copyValue;
  }

  public Integer getPriority() {
    return priority;
  }

  public String getStatus() {
    return status;
  }

}
