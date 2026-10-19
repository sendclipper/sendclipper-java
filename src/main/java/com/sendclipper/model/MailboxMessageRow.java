package com.sendclipper.model;

import java.time.Instant;

/** One message in a mailbox list. */
public class MailboxMessageRow extends ApiObject {
  private String id;
  private String folder;
  private String from;
  private String fromName;
  private String to;
  private String cc;
  private String bcc;
  private String subject;
  private String snippet;
  private String label;
  private boolean unread;
  private boolean starred;
  private Instant receivedAt;
  private String mailboxId;
  private String threadId;

  public String getId() {
    return id;
  }

  public String getFolder() {
    return folder;
  }

  public String getFrom() {
    return from;
  }

  public String getFromName() {
    return fromName;
  }

  public String getTo() {
    return to;
  }

  public String getCc() {
    return cc;
  }

  public String getBcc() {
    return bcc;
  }

  public String getSubject() {
    return subject;
  }

  public String getSnippet() {
    return snippet;
  }

  public String getLabel() {
    return label;
  }

  public boolean isUnread() {
    return unread;
  }

  public boolean isStarred() {
    return starred;
  }

  public Instant getReceivedAt() {
    return receivedAt;
  }

  public String getMailboxId() {
    return mailboxId;
  }

  public String getThreadId() {
    return threadId;
  }

}
