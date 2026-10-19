package com.sendclipper.model;

import java.util.List;

/** One cursor page of a mailbox. */
public class MailboxMessageList extends ApiObject {
  private List<MailboxMessageRow> items;
  private int total;
  private int unread;
  private boolean totalCapped;
  private CursorInfo page;

  public List<MailboxMessageRow> getItems() {
    return items;
  }

  public int getTotal() {
    return total;
  }

  public int getUnread() {
    return unread;
  }

  public boolean isTotalCapped() {
    return totalCapped;
  }

  public CursorInfo getPage() {
    return page;
  }

}
