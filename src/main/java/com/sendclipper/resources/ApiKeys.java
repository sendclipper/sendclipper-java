package com.sendclipper.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.ApiKey;
import java.util.List;

/** Lists API keys. Keys are created and revoked in the dashboard only. */
public final class ApiKeys {
  private static final TypeReference<List<ApiKey>> LIST = new TypeReference<List<ApiKey>>() {};

  private final HttpCore core;

  /**
   * The SDK makes these; use {@code client.apiKeys()}.
   *
   * @param core the engine
   */
  public ApiKeys(HttpCore core) {
    this.core = core;
  }

  /**
   * The workspace's keys, masked.
   *
   * @return the keys
   */
  public List<ApiKey> list() {
    return list(null);
  }

  /**
   * The keys, with per-call options.
   *
   * @param opts per-call options
   * @return the keys
   */
  public List<ApiKey> list(RequestOptions opts) {
    return core.json(ApiRequest.get("/api-keys"), opts, LIST);
  }
}
