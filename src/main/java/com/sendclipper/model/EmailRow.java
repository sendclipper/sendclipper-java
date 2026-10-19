package com.sendclipper.model;

import java.time.Instant;

/** One message in the log. */
public class EmailRow extends ApiObject {
  private String id;
  private String messageId;
  private String from;
  private String to;
  private String domain;
  private String subject;
  private EmailStatus status;
  private String sesMessageId;
  private Instant createdAt;

  public String getId() {
    return id;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public String getDomain() {
    return domain;
  }

  public String getSubject() {
    return subject;
  }

  public EmailStatus getStatus() {
    return status;
  }

  public String getSesMessageId() {
    return sesMessageId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

}
