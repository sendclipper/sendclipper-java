package com.sendclipper.model;

/** One address on a receiving domain. */
public class Mailbox extends ApiObject {
  private String id;
  private String address;
  private String displayName;
  private String domain;
  private String domainId;
  private boolean catchAll;
  private boolean archived;
  private boolean canReceive;
  private boolean canSend;
  private int unread;
  private int total;

  public String getId() {
    return id;
  }

  public String getAddress() {
    return address;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDomain() {
    return domain;
  }

  public String getDomainId() {
    return domainId;
  }

  public boolean isCatchAll() {
    return catchAll;
  }

  public boolean isArchived() {
    return archived;
  }

  public boolean canReceive() {
    return canReceive;
  }

  public boolean canSend() {
    return canSend;
  }

  public int getUnread() {
    return unread;
  }

  public int getTotal() {
    return total;
  }

}
