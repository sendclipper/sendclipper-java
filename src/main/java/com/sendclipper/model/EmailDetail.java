package com.sendclipper.model;

import java.util.List;

/** One message with its body and timeline. */
public class EmailDetail extends EmailRow {
  private String html;
  private String text;
  private boolean bodyExpired;
  private List<EmailEvent> events;
  private String threadId;
  private SendClass sendClass;
  private Boolean trackOpens;
  private Boolean trackClicks;

  public String getHtml() {
    return html;
  }

  public String getText() {
    return text;
  }

  /**
   * true once the retention window deleted the body.
   *
   * @return the body expired
   */
  public boolean isBodyExpired() {
    return bodyExpired;
  }

  public List<EmailEvent> getEvents() {
    return events;
  }

  public String getThreadId() {
    return threadId;
  }

  public SendClass getSendClass() {
    return sendClass;
  }

  public Boolean getTrackOpens() {
    return trackOpens;
  }

  public Boolean getTrackClicks() {
    return trackClicks;
  }

}
