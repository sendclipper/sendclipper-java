package com.sendclipper.model;

import java.util.List;

/** One cursor page of dispatches. */
public class WebhookDispatchPage extends ApiObject {
  private List<WebhookDispatch> data;
  private CursorInfo page;

  public List<WebhookDispatch> getData() {
    return data;
  }

  public CursorInfo getPage() {
    return page;
  }

}
