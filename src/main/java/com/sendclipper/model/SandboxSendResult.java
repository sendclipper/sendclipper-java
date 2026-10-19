package com.sendclipper.model;

/** What the sandbox JSON door answers. */
public class SandboxSendResult extends ApiObject {
  private String id;
  private boolean duplicate;

  public String getId() {
    return id;
  }

  public boolean isDuplicate() {
    return duplicate;
  }

}
