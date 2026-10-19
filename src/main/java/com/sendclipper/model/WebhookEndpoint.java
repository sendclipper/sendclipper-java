package com.sendclipper.model;

import java.time.Instant;
import java.util.List;

/** A URL SendClipper posts events to. */
public class WebhookEndpoint extends ApiObject {
  private String id;
  private String url;
  private List<WebhookEventType> events;
  private boolean active;
  private Instant createdAt;
  private String secretMasked;
  private Instant secretRotatedAt;
  private Instant previousSecretExpiresAt;
  private Double successRate;
  private int successRateWindowDays;
  private int deliveryCount;
  private int deliveredCount;

  public String getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public List<WebhookEventType> getEvents() {
    return events;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getSecretMasked() {
    return secretMasked;
  }

  public Instant getSecretRotatedAt() {
    return secretRotatedAt;
  }

  public Instant getPreviousSecretExpiresAt() {
    return previousSecretExpiresAt;
  }

  public Double getSuccessRate() {
    return successRate;
  }

  public int getSuccessRateWindowDays() {
    return successRateWindowDays;
  }

  public int getDeliveryCount() {
    return deliveryCount;
  }

  public int getDeliveredCount() {
    return deliveredCount;
  }

}
