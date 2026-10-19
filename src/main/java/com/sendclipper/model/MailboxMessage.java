package com.sendclipper.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** One message in full, with its conversation. */
public class MailboxMessage extends MailboxMessageRow {
  private String bodyText;
  private String bodyHtml;
  private Instant bodyExpiredAt;
  private Conversation thread;
  private List<AttachmentInfo> attachments;
  private Map<String, Object> authentication;

  public String getBodyText() {
    return bodyText;
  }

  public String getBodyHtml() {
    return bodyHtml;
  }

  public Instant getBodyExpiredAt() {
    return bodyExpiredAt;
  }

  /**
   * The conversation, or null.
   *
   * @return the thread
   */
  public Conversation getThread() {
    return thread;
  }

  /**
   * Download one with {@code mailboxes().messages().attachment(...)}.
   *
   * @return the attachments
   */
  public List<AttachmentInfo> getAttachments() {
    return attachments;
  }

  /**
   * SPF/DKIM/DMARC/spam verdicts for received mail; null for your own sends.
   *
   * @return the authentication
   */
  public Map<String, Object> getAuthentication() {
    return authentication;
  }

  /** The conversation a message belongs to. */
  public static class Conversation extends ApiObject {
    private String id;
    private String subject;
    private int messageCount;
    private Instant lastAt;
    private List<String> participants;
    private JsonNode messages;

    public String getId() {
      return id;
    }

    public String getSubject() {
      return subject;
    }

    public int getMessageCount() {
      return messageCount;
    }

    public Instant getLastAt() {
      return lastAt;
    }

    public List<String> getParticipants() {
      return participants;
    }

    public JsonNode getMessages() {
      return messages;
    }
  }

  /** One attachment of a message. */
  public static class AttachmentInfo extends ApiObject {
    private String id;
    private String filename;
    private String contentType;
    private long sizeBytes;

    public String getId() {
      return id;
    }

    public String getFilename() {
      return filename;
    }

    public String getContentType() {
      return contentType;
    }

    public long getSizeBytes() {
      return sizeBytes;
    }
  }
}
