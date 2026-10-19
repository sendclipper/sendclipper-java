package com.sendclipper.model;

import java.time.Instant;
import java.util.List;

/** A sending (and optionally receiving) domain. */
public class Domain extends ApiObject {
  private String id;
  private String name;
  private Region region;
  private String status;
  private boolean spfOk;
  private boolean dkimOk;
  private boolean dmarcOk;
  private List<RecordCheck> records;
  private int sent;
  private int checkCount;
  private Instant lastCheckedAt;
  private Instant createdAt;
  private String lastError;
  private boolean receivingEnabled;
  private boolean mxOk;
  private boolean receivingVerified;
  private String receivingError;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Region getRegion() {
    return region;
  }

  /**
   * {@code verified}, {@code pending} or {@code failed}.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  public boolean isSpfOk() {
    return spfOk;
  }

  public boolean isDkimOk() {
    return dkimOk;
  }

  public boolean isDmarcOk() {
    return dmarcOk;
  }

  public List<RecordCheck> getRecords() {
    return records;
  }

  public int getSent() {
    return sent;
  }

  public int getCheckCount() {
    return checkCount;
  }

  public Instant getLastCheckedAt() {
    return lastCheckedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getLastError() {
    return lastError;
  }

  public boolean isReceivingEnabled() {
    return receivingEnabled;
  }

  public boolean isMxOk() {
    return mxOk;
  }

  public boolean isReceivingVerified() {
    return receivingVerified;
  }

  public String getReceivingError() {
    return receivingError;
  }

  /** Whether one record answers. */
  public static class RecordCheck extends ApiObject {
    private String name;
    private boolean ok;

    public String getName() {
      return name;
    }

    public boolean isOk() {
      return ok;
    }
  }
}
