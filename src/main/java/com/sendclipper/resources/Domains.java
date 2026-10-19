package com.sendclipper.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.CreateDomainParams;
import com.sendclipper.model.Domain;
import com.sendclipper.model.DomainRecords;
import com.sendclipper.model.Region;
import java.util.Collections;
import java.util.List;

/** Manages sending and receiving domains. */
public final class Domains {
  private static final TypeReference<List<Domain>> LIST = new TypeReference<List<Domain>>() {};

  private final HttpCore core;

  /**
   * The SDK makes these; use {@code client.domains()}.
   *
   * @param core the engine
   */
  public Domains(HttpCore core) {
    this.core = core;
  }

  /**
   * Every domain.
   *
   * @return the domains
   */
  public List<Domain> list() {
    return list(null);
  }

  /**
   * Every domain, with per-call options.
   *
   * @param opts per-call options
   * @return the domains
   */
  public List<Domain> list(RequestOptions opts) {
    return core.json(ApiRequest.get("/domains"), opts, LIST);
  }

  /**
   * Registers a sending domain. Next: {@link #records(String)}, publish them, then {@link
   * #verify(String)}.
   *
   * @param params the domain
   * @return the domain
   */
  public Domain create(CreateDomainParams params) {
    return create(params, null);
  }

  /**
   * Registers a domain, with per-call options.
   *
   * @param params the domain
   * @param opts per-call options
   * @return the domain
   */
  public Domain create(CreateDomainParams params, RequestOptions opts) {
    return core.json(ApiRequest.post("/domains").body(params), opts, Domain.class);
  }

  /**
   * The DNS records to publish, each with its current status.
   *
   * @param id the domain's id
   * @return the domain and its records
   */
  public DomainRecords records(String id) {
    return records(id, null);
  }

  /**
   * The DNS records, with per-call options.
   *
   * @param id the domain's id
   * @param opts per-call options
   * @return the domain and its records
   */
  public DomainRecords records(String id, RequestOptions opts) {
    return core.json(ApiRequest.get(path(id) + "/records"), opts, DomainRecords.class);
  }

  /**
   * Checks DNS now instead of waiting for the background check.
   *
   * @param id the domain's id
   * @return the domain
   */
  public Domain verify(String id) {
    return verify(id, null);
  }

  /**
   * Checks DNS now, with per-call options.
   *
   * @param id the domain's id
   * @param opts per-call options
   * @return the domain
   */
  public Domain verify(String id, RequestOptions opts) {
    return core.json(ApiRequest.post(path(id) + "/verify"), opts, Domain.class);
  }

  /**
   * Starts receiving mail on the domain. It adds receiving records and never blocks sending.
   *
   * @param id the domain's id
   * @return the domain
   */
  public Domain enableReceiving(String id) {
    return enableReceiving(id, null);
  }

  /**
   * Starts receiving, with per-call options.
   *
   * @param id the domain's id
   * @param opts per-call options
   * @return the domain
   */
  public Domain enableReceiving(String id, RequestOptions opts) {
    return core.json(ApiRequest.post(path(id) + "/receiving"), opts, Domain.class);
  }

  /**
   * Stops receiving mail on the domain.
   *
   * @param id the domain's id
   * @return the domain
   */
  public Domain disableReceiving(String id) {
    return disableReceiving(id, null);
  }

  /**
   * Stops receiving, with per-call options.
   *
   * @param id the domain's id
   * @param opts per-call options
   * @return the domain
   */
  public Domain disableReceiving(String id, RequestOptions opts) {
    return core.json(ApiRequest.delete(path(id) + "/receiving"), opts, Domain.class);
  }

  /**
   * Moves a domain to another region. Refused ({@code region_immutable}) once it holds messages.
   *
   * @param id the domain's id
   * @param region the new region
   * @return the domain
   */
  public Domain setRegion(String id, Region region) {
    return setRegion(id, region, null);
  }

  /**
   * Moves a domain, with per-call options.
   *
   * @param id the domain's id
   * @param region the new region
   * @param opts per-call options
   * @return the domain
   */
  public Domain setRegion(String id, Region region, RequestOptions opts) {
    return core.json(
        ApiRequest.patch(path(id) + "/region").body(Collections.singletonMap("region", region)),
        opts,
        Domain.class);
  }

  /**
   * What deleting the domain would take with it.
   *
   * @param id the domain's id
   * @return the preview
   */
  public JsonNode deletionPreview(String id) {
    return deletionPreview(id, null);
  }

  /**
   * The deletion preview, with per-call options.
   *
   * @param id the domain's id
   * @param opts per-call options
   * @return the preview
   */
  public JsonNode deletionPreview(String id, RequestOptions opts) {
    return core.json(ApiRequest.get(path(id) + "/deletion"), opts, JsonNode.class);
  }

  /**
   * Deletes a domain.
   *
   * @param id the domain's id
   */
  public void remove(String id) {
    remove(id, null);
  }

  /**
   * Deletes a domain, with per-call options.
   *
   * @param id the domain's id
   * @param opts per-call options
   */
  public void remove(String id, RequestOptions opts) {
    core.discard(ApiRequest.delete(path(id)), opts);
  }

  private static String path(String id) {
    return "/domains/" + ApiRequest.seg(id);
  }
}
