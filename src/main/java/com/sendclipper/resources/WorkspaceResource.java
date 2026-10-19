package com.sendclipper.resources;

import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.Workspace;

/** Reads the key's workspace. */
public final class WorkspaceResource {
  private final HttpCore core;

  /**
   * The SDK makes these; use {@code client.workspace()}.
   *
   * @param core the engine
   */
  public WorkspaceResource(HttpCore core) {
    this.core = core;
  }

  /**
   * The workspace this key belongs to.
   *
   * @return the workspace
   */
  public Workspace get() {
    return get(null);
  }

  /**
   * The workspace, with per-call options.
   *
   * @param opts per-call options
   * @return the workspace
   */
  public Workspace get(RequestOptions opts) {
    return core.json(ApiRequest.get("/workspace"), opts, Workspace.class);
  }
}
