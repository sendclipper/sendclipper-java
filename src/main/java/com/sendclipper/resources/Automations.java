package com.sendclipper.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import java.util.Collections;

/** Starts automations. */
public final class Automations {
  private final HttpCore core;

  /**
   * The SDK makes these; use {@code client.automations()}.
   *
   * @param core the engine
   */
  public Automations(HttpCore core) {
    this.core = core;
  }

  /**
   * Starts a Live automation with a manual trigger for one contact.
   *
   * @param automationId the automation's id
   * @param email the contact's address
   * @return the enrollment
   */
  public JsonNode enroll(String automationId, String email) {
    return enroll(automationId, email, null);
  }

  /**
   * Enrolls a contact, with per-call options.
   *
   * @param automationId the automation's id
   * @param email the contact's address
   * @param opts per-call options
   * @return the enrollment
   */
  public JsonNode enroll(String automationId, String email, RequestOptions opts) {
    return core.json(
        ApiRequest.post("/automations/" + ApiRequest.seg(automationId) + "/enrollments")
            .body(Collections.singletonMap("email", email)),
        opts,
        JsonNode.class);
  }
}
