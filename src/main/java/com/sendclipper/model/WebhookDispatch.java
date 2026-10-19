package com.sendclipper.model;

import java.time.Instant;

/** One event's delivery to one endpoint. */
public class WebhookDispatch extends ApiObject {
  private String id;
  private String endpointId;
  private String endpointUrl;
  private String eventId;
  private WebhookEventType event;
  private String state;
  private int attempts;
  private Integer lastStatusCode;
  private String lastError;
  private String lastAttemptError;
  private Instant firstAttemptAt;
  private Instant lastAttemptAt;
  private Instant nextAttemptAt;
  private Instant createdAt;
  private Instant replayedAt;
  private int replayCount;

  public String getId() {
    return id;
  }

  public String getEndpointId() {
    return endpointId;
  }

  public String getEndpointUrl() {
    return endpointUrl;
  }

  /**
   * The id the receiver saw in the body.
   *
   * @return the event id
   */
  public String getEventId() {
    return eventId;
  }

  public WebhookEventType getEvent() {
    return event;
  }

  /**
   * {@code pending}, {@code delivering}, {@code delivered} or {@code dead_lettered}.
   *
   * @return the state
   */
  public String getState() {
    return state;
  }

  public int getAttempts() {
    return attempts;
  }

  public Integer getLastStatusCode() {
    return lastStatusCode;
  }

  public String getLastError() {
    return lastError;
  }

  public String getLastAttemptError() {
    return lastAttemptError;
  }

  public Instant getFirstAttemptAt() {
    return firstAttemptAt;
  }

  public Instant getLastAttemptAt() {
    return lastAttemptAt;
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getReplayedAt() {
    return replayedAt;
  }

  public int getReplayCount() {
    return replayCount;
  }

}
