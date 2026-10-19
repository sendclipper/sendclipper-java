package com.sendclipper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/** A webhook delivery's body. Get one from {@code Webhook.verify(...)}. */
public class WebhookEvent extends ApiObject {
  private String id;

  @JsonProperty("event")
  private String eventName;

  private Instant createdAt;
  private WebhookEventData data;

  /**
   * The event's id, stable for one (email, event): deduplicate on it.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * The event type.
   *
   * @return the type, or {@link WebhookEventType#UNKNOWN} for one this SDK does not know yet (see
   *     {@link #getEventName()})
   */
  public WebhookEventType getEvent() {
    for (WebhookEventType t : WebhookEventType.values()) {
      if (t.getValue().equals(eventName)) {
        return t;
      }
    }
    return WebhookEventType.UNKNOWN;
  }

  /**
   * The event type as sent, e.g. {@code email.delivered}.
   *
   * @return the raw name
   */
  public String getEventName() {
    return eventName;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  /**
   * The message the event is about.
   *
   * @return the data, or null when the body carried none
   */
  public WebhookEventData getData() {
    return data;
  }
}
