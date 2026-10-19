package com.sendclipper.model;

import java.time.Instant;

/** Traps mail: nothing sent to it is delivered. */
public class Sandbox extends ApiObject {
  private String id;
  private String name;
  private String slug;
  private String inboxAddress;
  private boolean inboundLive;
  private String projectLabel;
  private String color;
  private int total;
  private int unread;
  private int messageCap;
  private int retentionDays;
  private Instant lastTrappedAt;
  private Instant createdAt;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSlug() {
    return slug;
  }

  /**
   * Mail sent to this address lands in the sandbox.
   *
   * @return the inbox address
   */
  public String getInboxAddress() {
    return inboxAddress;
  }

  public boolean isInboundLive() {
    return inboundLive;
  }

  public String getProjectLabel() {
    return projectLabel;
  }

  public String getColor() {
    return color;
  }

  public int getTotal() {
    return total;
  }

  public int getUnread() {
    return unread;
  }

  public int getMessageCap() {
    return messageCap;
  }

  public int getRetentionDays() {
    return retentionDays;
  }

  public Instant getLastTrappedAt() {
    return lastTrappedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

}
