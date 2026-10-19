package com.sendclipper.model;

import java.util.List;

/** Every audience. */
public class AudienceList extends ApiObject {
  private List<Audience> audiences;
  private int totalContacts;

  public List<Audience> getAudiences() {
    return audiences;
  }

  public int getTotalContacts() {
    return totalContacts;
  }

}
