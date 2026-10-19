package com.sendclipper.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sendclipper.RequestOptions;
import com.sendclipper.Webhook;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.CreateWebhookParams;
import com.sendclipper.model.ListWebhookDeliveriesParams;
import com.sendclipper.model.UpdateWebhookParams;
import com.sendclipper.model.WebhookEndpoint;
import com.sendclipper.model.WebhookEndpointWithSecret;
import com.sendclipper.model.WebhookEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Manages webhook endpoints. */
public final class Webhooks {
  private static final TypeReference<List<WebhookEndpoint>> LIST =
      new TypeReference<List<WebhookEndpoint>>() {};
  private static final TypeReference<List<JsonNode>> DELIVERIES =
      new TypeReference<List<JsonNode>>() {};

  private final HttpCore core;
  private final WebhookDispatches dispatches;

  /**
   * The SDK makes these; use {@code client.webhooks()}.
   *
   * @param core the engine
   */
  public Webhooks(HttpCore core) {
    this.core = core;
    this.dispatches = new WebhookDispatches(core);
  }

  /**
   * Undelivered events, and replaying them.
   *
   * @return the dispatches resource
   */
  public WebhookDispatches dispatches() {
    return dispatches;
  }

  /**
   * Every endpoint.
   *
   * @return the endpoints
   */
  public List<WebhookEndpoint> list() {
    return list(null);
  }

  /**
   * Every endpoint, with per-call options.
   *
   * @param opts per-call options
   * @return the endpoints
   */
  public List<WebhookEndpoint> list(RequestOptions opts) {
    return core.json(ApiRequest.get("/webhook-endpoints"), opts, LIST);
  }

  /**
   * Registers an endpoint. The response carries the signing secret once: store it.
   *
   * @param params the endpoint
   * @return the endpoint with its secret
   */
  public WebhookEndpointWithSecret create(CreateWebhookParams params) {
    return create(params, null);
  }

  /**
   * Registers an endpoint, with per-call options.
   *
   * @param params the endpoint
   * @param opts per-call options
   * @return the endpoint with its secret
   */
  public WebhookEndpointWithSecret create(CreateWebhookParams params, RequestOptions opts) {
    return core.json(
        ApiRequest.post("/webhook-endpoints").body(params), opts, WebhookEndpointWithSecret.class);
  }

  /**
   * Changes the fields set in params.
   *
   * @param id the endpoint's id
   * @param params the changes
   * @return the endpoint
   */
  public WebhookEndpoint update(String id, UpdateWebhookParams params) {
    return update(id, params, null);
  }

  /**
   * Changes an endpoint, with per-call options.
   *
   * @param id the endpoint's id
   * @param params the changes
   * @param opts per-call options
   * @return the endpoint
   */
  public WebhookEndpoint update(String id, UpdateWebhookParams params, RequestOptions opts) {
    return core.json(ApiRequest.patch(path(id)).body(params), opts, WebhookEndpoint.class);
  }

  /**
   * Deletes an endpoint.
   *
   * @param id the endpoint's id
   */
  public void remove(String id) {
    remove(id, null);
  }

  /**
   * Deletes an endpoint, with per-call options.
   *
   * @param id the endpoint's id
   * @param opts per-call options
   */
  public void remove(String id, RequestOptions opts) {
    core.discard(ApiRequest.delete(path(id)), opts);
  }

  /**
   * Makes a new secret. The old one keeps signing alongside it for the grace window.
   *
   * @param id the endpoint's id
   * @return the endpoint with its new secret
   */
  public WebhookEndpointWithSecret rotateSecret(String id) {
    return rotateSecret(id, null);
  }

  /**
   * Rotates the secret, with per-call options.
   *
   * @param id the endpoint's id
   * @param opts per-call options
   * @return the endpoint with its new secret
   */
  public WebhookEndpointWithSecret rotateSecret(String id, RequestOptions opts) {
    return core.json(
        ApiRequest.post(path(id) + "/rotate-secret"), opts, WebhookEndpointWithSecret.class);
  }

  /**
   * Recent delivery attempts.
   *
   * @param params filters, or null
   * @return the attempts
   */
  public List<JsonNode> deliveries(ListWebhookDeliveriesParams params) {
    return deliveries(params, null);
  }

  /**
   * Recent delivery attempts, with per-call options.
   *
   * @param params filters, or null
   * @param opts per-call options
   * @return the attempts
   */
  public List<JsonNode> deliveries(ListWebhookDeliveriesParams params, RequestOptions opts) {
    ApiRequest req = ApiRequest.get("/webhook-deliveries");
    if (params != null) {
      req.query("endpointId", params.getEndpointId())
          .query("finalOnly", params.getFinalOnly())
          .query("limit", params.getLimit());
    }
    return core.json(req, opts, DELIVERIES);
  }

  /**
   * The same as {@link Webhook#verify(byte[], Map, String)}. It makes no request.
   *
   * @param payload the body exactly as received
   * @param headers the request's headers
   * @param secret the endpoint's signing secret
   * @return the verified event
   */
  public WebhookEvent verify(
      byte[] payload, Map<String, ? extends Collection<String>> headers, String secret) {
    return Webhook.verify(payload, headers, secret);
  }

  private static String path(String id) {
    return "/webhook-endpoints/" + ApiRequest.seg(id);
  }
}
