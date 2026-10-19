package com.sendclipper.resources;

import com.sendclipper.PageIterable;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.core.Paging;
import com.sendclipper.model.CreateSuppressionParams;
import com.sendclipper.model.ListSuppressionsParams;
import com.sendclipper.model.Suppression;
import com.sendclipper.model.SuppressionCreated;
import com.sendclipper.model.SuppressionList;
import java.util.Collections;

/** Manages the suppression list. */
public final class Suppressions {
  private final HttpCore core;

  /**
   * The SDK makes these; use {@code client.suppressions()}.
   *
   * @param core the engine
   */
  public Suppressions(HttpCore core) {
    this.core = core;
  }

  /**
   * One cursor page of the list.
   *
   * @param params filters, or null
   * @return the page
   */
  public SuppressionList list(ListSuppressionsParams params) {
    return list(params, null);
  }

  /**
   * One cursor page, with per-call options.
   *
   * @param params filters, or null
   * @param opts per-call options
   * @return the page
   */
  public SuppressionList list(ListSuppressionsParams params, RequestOptions opts) {
    return list(params, params == null ? null : params.getCursor(), opts);
  }

  private SuppressionList list(ListSuppressionsParams p, String cursor, RequestOptions opts) {
    ApiRequest req = ApiRequest.get("/suppressions");
    if (p != null) {
      req.query("q", p.getQ()).query("reason", p.getReason()).query("limit", p.getLimit());
    }
    req.query("cursor", cursor);
    return core.json(req, opts, SuppressionList.class);
  }

  /**
   * Every suppression matching params, following the cursor.
   *
   * @param params filters, or null; its cursor is ignored
   * @return the suppressions
   */
  public PageIterable<Suppression> all(ListSuppressionsParams params) {
    return all(params, null);
  }

  /**
   * Every matching suppression, with per-call options for each page.
   *
   * @param params filters, or null; its cursor is ignored
   * @param opts per-call options
   * @return the suppressions
   */
  public PageIterable<Suppression> all(ListSuppressionsParams params, RequestOptions opts) {
    return Paging.cursor(
        cursor -> {
          SuppressionList res = list(params, cursor, opts);
          return new Paging.Cursored<>(res.getData(), res.getPage());
        });
  }

  /**
   * Stops all mail to an address (reason {@code manual}).
   *
   * @param params the address
   * @return the entry, and whether it was new
   */
  public SuppressionCreated create(CreateSuppressionParams params) {
    return create(params, null);
  }

  /**
   * Suppresses an address, with per-call options.
   *
   * @param params the address
   * @param opts per-call options
   * @return the entry, and whether it was new
   */
  public SuppressionCreated create(CreateSuppressionParams params, RequestOptions opts) {
    return core.json(ApiRequest.post("/suppressions").body(params), opts, SuppressionCreated.class);
  }

  /**
   * Lifts a suppression. Complaint and provider suppressions cannot be removed ({@code
   * suppression_not_removable}).
   *
   * @param id the suppression's id
   * @param reason why (10+ characters); it is recorded
   */
  public void remove(String id, String reason) {
    remove(id, reason, null);
  }

  /**
   * Lifts a suppression, with per-call options.
   *
   * @param id the suppression's id
   * @param reason why (10+ characters)
   * @param opts per-call options
   */
  public void remove(String id, String reason, RequestOptions opts) {
    core.discard(
        ApiRequest.delete("/suppressions/" + ApiRequest.seg(id))
            .body(Collections.singletonMap("reason", reason)),
        opts);
  }

  /**
   * The list as CSV. The cursor and limit are ignored.
   *
   * @param params filters, or null
   * @return the CSV text
   */
  public String export(ListSuppressionsParams params) {
    return export(params, null);
  }

  /**
   * The list as CSV, with per-call options.
   *
   * @param params filters, or null
   * @param opts per-call options
   * @return the CSV text
   */
  public String export(ListSuppressionsParams params, RequestOptions opts) {
    ApiRequest req = ApiRequest.get("/suppressions/export");
    if (params != null) {
      req.query("q", params.getQ()).query("reason", params.getReason());
    }
    return core.text(req, opts);
  }
}
