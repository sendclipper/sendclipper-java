package com.sendclipper.model;

import java.time.Instant;

/** The workspace an API key belongs to. */
public class Workspace extends ApiObject {
  private String id;
  private String name;
  private String publicId;
  private Instant createdAt;
  private SendingPaused sendingPaused;
  private String postalAddress;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getPublicId() {
    return publicId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  /**
   * Why sending is paused, or null.
   *
   * @return the sending paused
   */
  public SendingPaused getSendingPaused() {
    return sendingPaused;
  }

  public String getPostalAddress() {
    return postalAddress;
  }

  /** Why sending is paused. */
  public static class SendingPaused extends ApiObject {
    private Instant at;
    private String reason;
    private String message;

    public Instant getAt() {
      return at;
    }

    public String getReason() {
      return reason;
    }

    public String getMessage() {
      return message;
    }
  }
}
