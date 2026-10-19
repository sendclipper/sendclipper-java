package com.sendclipper.model;

import java.util.List;

/** One result per submitted item, in order. */
public class SendBatchResponse extends ApiObject {
  private List<BatchResult> data;
  private Summary summary;

  public List<BatchResult> getData() {
    return data;
  }

  public Summary getSummary() {
    return summary;
  }

  /** How many items were submitted, accepted and refused. */
  public static class Summary extends ApiObject {
    private int submitted;
    private int accepted;
    private int failed;

    public int getSubmitted() {
      return submitted;
    }

    public int getAccepted() {
      return accepted;
    }

    public int getFailed() {
      return failed;
    }
  }
}
