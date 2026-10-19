package com.sendclipper.model;

import java.time.Instant;
import java.util.List;

/** One trapped message in a list. */
public class SandboxMessageSummary extends ApiObject {
  private String id;
  private String subject;
  private String fromAddress;
  private String fromName;
  private List<String> toAddresses;
  private String snippet;
  private long sizeBytes;
  private boolean read;
  private Instant trappedAt;

  public String getId() {
    return id;
  }

  public String getSubject() {
    return subject;
  }

  public String getFromAddress() {
    return fromAddress;
  }

  public String getFromName() {
    return fromName;
  }

  public List<String> getToAddresses() {
    return toAddresses;
  }

  public String getSnippet() {
    return snippet;
  }

  public long getSizeBytes() {
    return sizeBytes;
  }

  public boolean isRead() {
    return read;
  }

  public Instant getTrappedAt() {
    return trappedAt;
  }

}
