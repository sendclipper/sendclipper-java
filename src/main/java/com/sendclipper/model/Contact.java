package com.sendclipper.model;

import java.time.Instant;

/** One person in an audience. */
public class Contact extends ApiObject {
  private String id;
  private String name;
  private String email;
  private String status;
  private String audienceId;
  private String audienceName;
  private String audienceColor;
  private Instant createdAt;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  /**
   * {@code subscribed}, {@code unsubscribed} or {@code bounced}.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  public String getAudienceId() {
    return audienceId;
  }

  public String getAudienceName() {
    return audienceName;
  }

  public String getAudienceColor() {
    return audienceColor;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

}
