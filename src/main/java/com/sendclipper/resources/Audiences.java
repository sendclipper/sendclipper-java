package com.sendclipper.resources;

import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.AudienceList;

/** Lists audiences. */
public final class Audiences {
  private final HttpCore core;

  /**
   * The SDK makes these; use {@code client.audiences()}.
   *
   * @param core the engine
   */
  public Audiences(HttpCore core) {
    this.core = core;
  }

  /**
   * Every audience and the total contact count.
   *
   * @return the audiences
   */
  public AudienceList list() {
    return list(null);
  }

  /**
   * Every audience, with per-call options.
   *
   * @param opts per-call options
   * @return the audiences
   */
  public AudienceList list(RequestOptions opts) {
    return core.json(ApiRequest.get("/audiences"), opts, AudienceList.class);
  }
}
