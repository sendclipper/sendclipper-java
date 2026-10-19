package com.sendclipper.model;

import java.util.List;
import java.util.Map;

/** One cursor page of the list. */
public class SuppressionList extends ApiObject {
  private List<Suppression> data;
  private CursorInfo page;
  private Counts counts;

  public List<Suppression> getData() {
    return data;
  }

  public CursorInfo getPage() {
    return page;
  }

  public Counts getCounts() {
    return counts;
  }

  /** How many entries the list holds, per reason. */
  public static class Counts extends ApiObject {
    private int total;
    private Map<String, Integer> byReason;

    public int getTotal() {
      return total;
    }

    public Map<String, Integer> getByReason() {
      return byReason;
    }
  }
}
