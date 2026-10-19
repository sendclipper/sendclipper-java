package com.sendclipper.model;

import java.time.Instant;

/** Stops mail to one address. */
public class Suppression extends ApiObject {
  private String id;
  private String email;
  private String reason;
  private String reasonLabel;
  private String reasonDetail;
  private String diagnosticCode;
  private String scope;
  private String sourceEmailId;
  private String sourceMessageId;
  private Instant createdAt;

  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  /**
   * {@code hard_bounce}, {@code soft_bounce}, {@code complaint}, {@code unsubscribe}, {@code manual} or {@code provider}.
   *
   * @return the reason
   */
  public String getReason() {
    return reason;
  }

  public String getReasonLabel() {
    return reasonLabel;
  }

  public String getReasonDetail() {
    return reasonDetail;
  }

  public String getDiagnosticCode() {
    return diagnosticCode;
  }

  /**
   * {@code marketing} for an unsubscribe, {@code all} for everything else.
   *
   * @return the scope
   */
  public String getScope() {
    return scope;
  }

  public String getSourceEmailId() {
    return sourceEmailId;
  }

  public String getSourceMessageId() {
    return sourceMessageId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

}
