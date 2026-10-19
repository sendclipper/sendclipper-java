package com.sendclipper.model;

/** What a bulk replay queued. */
public class ReplayAllResult extends ApiObject {
  private int queued;
  private int remaining;
  private boolean capped;
  private int max;

  public int getQueued() {
    return queued;
  }

  public int getRemaining() {
    return remaining;
  }

  public boolean isCapped() {
    return capped;
  }

  public int getMax() {
    return max;
  }

}
