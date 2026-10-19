package com.sendclipper.model;

import java.time.Instant;

/** One of the workspace's keys, masked. */
public class ApiKey extends ApiObject {
  private String id;
  private String name;
  private String masked;
  private String prefix;
  private String last4;
  private String permission;
  private String domainId;
  private String domain;
  private Instant createdAt;
  private Instant lastUsedAt;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getMasked() {
    return masked;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getLast4() {
    return last4;
  }

  /**
   * {@code full}, {@code sending} or {@code read}.
   *
   * @return the permission
   */
  public String getPermission() {
    return permission;
  }

  public String getDomainId() {
    return domainId;
  }

  public String getDomain() {
    return domain;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getLastUsedAt() {
    return lastUsedAt;
  }

}
