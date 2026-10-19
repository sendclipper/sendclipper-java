package com.sendclipper.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** The message a webhook event is about. {@link #getRaw()} holds every field. */
public class WebhookEventData extends ApiObject {
  private String emailId;
  private String messageId;
  private String to;
  private String domain;
  private String subject;
  @JsonIgnore private final Map<String, Object> raw = new LinkedHashMap<>();

  public String getEmailId() {
    return emailId;
  }

  public String getMessageId() {
    return messageId;
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

  /**
   * The whole data object, for event-specific fields (bounce type, clicked link, ...).
   *
   * @return every field, typed ones included
   */
  public Map<String, Object> getRaw() {
    return Collections.unmodifiableMap(raw);
  }

  @JsonProperty("emailId")
  private void setEmailId(String v) {
    emailId = v;
    raw.put("emailId", v);
  }

  @JsonProperty("messageId")
  private void setMessageId(String v) {
    messageId = v;
    raw.put("messageId", v);
  }

  @JsonProperty("to")
  private void setTo(String v) {
    to = v;
    raw.put("to", v);
  }

  @JsonProperty("domain")
  private void setDomain(String v) {
    domain = v;
    raw.put("domain", v);
  }

  @JsonProperty("subject")
  private void setSubject(String v) {
    subject = v;
    raw.put("subject", v);
  }

  @JsonAnySetter
  private void setOther(String name, Object value) {
    raw.put(name, value);
  }
}
