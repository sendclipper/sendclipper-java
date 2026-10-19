package com.sendclipper.resources;

import com.sendclipper.PageIterable;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.core.Paging;
import com.sendclipper.model.ListWebhookDispatchesParams;
import com.sendclipper.model.ReplayAllParams;
import com.sendclipper.model.ReplayAllResult;
import com.sendclipper.model.WebhookDispatch;
import com.sendclipper.model.WebhookDispatchPage;
import java.util.Collections;

/** Reads and replays undelivered webhook events. */
public final class WebhookDispatches {
  private final HttpCore core;

  WebhookDispatches(HttpCore core) {
    this.core = core;
  }

  /**
   * Undelivered events: dead letters by default, or state {@code retrying}.
   *
   * @param params filters, or null
   * @return one cursor page
   */
  public WebhookDispatchPage list(ListWebhookDispatchesParams params) {
    return list(params, null);
  }

  /**
   * Undelivered events, with per-call options.
   *
   * @param params filters, or null
   * @param opts per-call options
   * @return one cursor page
   */
  public WebhookDispatchPage list(ListWebhookDispatchesParams params, RequestOptions opts) {
    return list(params, params == null ? null : params.getCursor(), opts);
  }

  private WebhookDispatchPage list(
      ListWebhookDispatchesParams p, String cursor, RequestOptions opts) {
    ApiRequest req = ApiRequest.get("/webhook-dispatches");
    if (p != null) {
      req.query("state", p.getState()).query("endpointId", p.getEndpointId());
    }
    req.query("cursor", cursor).query("limit", p == null ? null : p.getLimit());
    return core.json(req, opts, WebhookDispatchPage.class);
  }

  /**
   * Every dispatch matching params, following the cursor.
   *
   * @param params filters, or null; its cursor is ignored
   * @return the dispatches
   */
  public PageIterable<WebhookDispatch> all(ListWebhookDispatchesParams params) {
    return all(params, null);
  }

  /**
   * Every matching dispatch, with per-call options for each page.
   *
   * @param params filters, or null; its cursor is ignored
   * @param opts per-call options
   * @return the dispatches
   */
  public PageIterable<WebhookDispatch> all(
      ListWebhookDispatchesParams params, RequestOptions opts) {
    return Paging.cursor(
        cursor -> {
          WebhookDispatchPage res = list(params, cursor, opts);
          return new Paging.Cursored<>(res.getData(), res.getPage());
        });
  }

  /**
   * One dispatch.
   *
   * @param id the dispatch's id
   * @return the dispatch
   */
  public WebhookDispatch get(String id) {
    return get(id, null);
  }

  /**
   * One dispatch, with per-call options.
   *
   * @param id the dispatch's id
   * @param opts per-call options
   * @return the dispatch
   */
  public WebhookDispatch get(String id, RequestOptions opts) {
    return core.json(
        ApiRequest.get("/webhook-dispatches/" + ApiRequest.seg(id)), opts, WebhookDispatch.class);
  }

  /**
   * Makes one more immediate attempt for a dead-lettered event.
   *
   * @param id the dispatch's id
   * @return the dispatch
   */
  public WebhookDispatch replay(String id) {
    return replay(id, null);
  }

  /**
   * Replays one event, with per-call options.
   *
   * @param id the dispatch's id
   * @param opts per-call options
   * @return the dispatch
   */
  public WebhookDispatch replay(String id, RequestOptions opts) {
    return core.json(
        ApiRequest.post("/webhook-dispatches/" + ApiRequest.seg(id) + "/replay"),
        opts,
        WebhookDispatch.class);
  }

  /**
   * Replays every dead letter (optionally of one endpoint), up to the server's cap.
   *
   * @param params the scope, or null for every dead letter
   * @return what was queued
   */
  public ReplayAllResult replayAll(ReplayAllParams params) {
    return replayAll(params, null);
  }

  /**
   * Replays every dead letter, with per-call options.
   *
   * @param params the scope, or null
   * @param opts per-call options
   * @return what was queued
   */
  public ReplayAllResult replayAll(ReplayAllParams params, RequestOptions opts) {
    Object body = params == null ? Collections.emptyMap() : params;
    return core.json(
        ApiRequest.post("/webhook-dispatches/replay-all").body(body), opts, ReplayAllResult.class);
  }
}
