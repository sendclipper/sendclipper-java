package com.sendclipper.model;

/** The paging block of a cursor-addressed list. */
public class CursorInfo extends ApiObject {
  private String cursor;
  private boolean hasMore;

  /**
   * The cursor of the next page, or null.
   *
   * @return the cursor
   */
  public String getCursor() {
    return cursor;
  }

  /**
   * Whether another page follows.
   *
   * @return the has more
   */
  public boolean hasMore() {
    return hasMore;
  }

}
