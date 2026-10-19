package com.sendclipper.model;

import java.time.Instant;

/** What {@code emails().send(...)} answers: the message was accepted (queued). */
public class SentEmail extends ApiObject {
  private String id;
  private String messageId;
  private String sesMessageId;
  private String from;
  private String to;
  private String domain;
  private String subject;
  private EmailStatus status;
  private Instant createdAt;
  private String templateId;

  /**
   * SendClipper's id for the message. Use it with {@code emails().get(id)}.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getSesMessageId() {
    return sesMessageId;
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getTemplateId() {
    return templateId;
  }

}
