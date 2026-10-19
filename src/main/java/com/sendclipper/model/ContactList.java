package com.sendclipper.model;

import java.util.List;

/** One numbered page of contacts. */
public class ContactList extends ApiObject {
  private List<Contact> items;
  private int total;
  private int page;
  private int pageSize;
  private int totalPages;
  private int totalAll;

  public List<Contact> getItems() {
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

  public int getTotalPages() {
    return totalPages;
  }

  public int getTotalAll() {
    return totalAll;
  }

}
