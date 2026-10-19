package com.sendclipper.model;

/** Counts messages per status. */
public class EmailStats extends ApiObject {
  private int total;
  private int queued;
  private int sent;
  private int delivered;
  private int opened;
  private int clicked;
  private int bounced;
  private int complained;
  private int failed;
  private int bodyRetentionDays;

  public int getTotal() {
    return total;
  }

  public int getQueued() {
    return queued;
  }

  public int getSent() {
    return sent;
  }

  public int getDelivered() {
    return delivered;
  }

  public int getOpened() {
    return opened;
  }

  public int getClicked() {
    return clicked;
  }

  public int getBounced() {
    return bounced;
  }

  public int getComplained() {
    return complained;
  }

  public int getFailed() {
    return failed;
  }

  public int getBodyRetentionDays() {
    return bodyRetentionDays;
  }

}
