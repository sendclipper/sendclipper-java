package com.sendclipper.model;

import java.util.List;

/** One numbered page of the log. */
public class EmailList extends ApiObject {
  private List<EmailRow> items;
  private int total;
  private int page;
  private int pageSize;
  private boolean totalCapped;

  public List<EmailRow> getItems() {
    return items;
  }

  public int getTotal() {
    return total;
  }

  public int getPage() {
    return page;
  }

  public int getPageSize() {
    return pageSize;
  }

  /**
   * A search counts only its newest 1,000 matches.
   *
   * @return the total capped
   */
  public boolean isTotalCapped() {
    return totalCapped;
  }

}
