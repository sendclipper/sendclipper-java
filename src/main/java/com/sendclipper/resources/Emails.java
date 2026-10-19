package com.sendclipper.resources;

import com.sendclipper.PageIterable;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.core.Paging;
import com.sendclipper.model.EmailDetail;
import com.sendclipper.model.EmailList;
import com.sendclipper.model.EmailRow;
import com.sendclipper.model.EmailStats;
import com.sendclipper.model.ListEmailsParams;
import com.sendclipper.model.SendBatchResponse;
import com.sendclipper.model.SendEmailParams;
import com.sendclipper.model.SentEmail;
import java.util.List;

/** Sends mail and reads the sent-mail log. */
public final class Emails {
  private final HttpCore core;

  /**
   * The SDK makes these; use {@code client.emails()}.
   *
   * @param core the engine
   */
  public Emails(HttpCore core) {
    this.core = core;
  }

  /**
   * Sends one message. It returns once SendClipper accepted it (status {@code queued}); delivery
   * is reported later through {@link #get(String)} and webhooks.
   *
   * <p>Every call carries an Idempotency-Key, so the SDK's own retries never send twice. Pass
   * {@link RequestOptions.Builder#idempotencyKey(String)} to make YOUR retries safe too.
   *
   * @param params the message
   * @return the accepted message
   */
  public SentEmail send(SendEmailParams params) {
    return send(params, null);
  }

  /**
   * Sends one message, with per-call options.
   *
   * @param params the message
   * @param opts per-call options
   * @return the accepted message
   */
  public SentEmail send(SendEmailParams params, RequestOptions opts) {
    return core.json(ApiRequest.post("/emails").body(params), opts, SentEmail.class);
  }

  /**
   * The same as {@code batch().send(emails)}.
   *
   * @param emails up to 500 messages
   * @return one result per item
   */
  public SendBatchResponse sendBatch(List<SendEmailParams> emails) {
    return Batch.send(core, emails, null);
  }

  /**
   * The same as {@code batch().send(emails, opts)}.
   *
   * @param emails up to 500 messages
   * @param opts per-call options
   * @return one result per item
   */
  public SendBatchResponse sendBatch(List<SendEmailParams> emails, RequestOptions opts) {
    return Batch.send(core, emails, opts);
  }

  /**
   * One message with its body and event timeline.
   *
   * @param id the message's id
   * @return the message
   */
  public EmailDetail get(String id) {
    return get(id, null);
  }

  /**
   * One message, with per-call options.
   *
   * @param id the message's id
   * @param opts per-call options
   * @return the message
   */
  public EmailDetail get(String id, RequestOptions opts) {
    return core.json(ApiRequest.get("/emails/" + ApiRequest.seg(id)), opts, EmailDetail.class);
  }

  /**
   * One page of the sent-mail log, newest first.
   *
   * @param params filters, or null
   * @return the page
   */
  public EmailList list(ListEmailsParams params) {
    return list(params, null);
  }

  /**
   * One page of the log, with per-call options.
   *
   * @param params filters, or null
   * @param opts per-call options
   * @return the page
   */
  public EmailList list(ListEmailsParams params, RequestOptions opts) {
    Integer page = params == null ? null : params.getPage();
    Integer size = params == null ? null : params.getPageSize();
    return list(params, page, size, opts);
  }

  private EmailList list(ListEmailsParams p, Integer page, Integer size, RequestOptions opts) {
    ApiRequest req = ApiRequest.get("/emails");
    if (p != null) {
      req.query("search", p.getSearch())
          .query("status", p.getStatus() == null ? null : p.getStatus().getValue())
          .query("domain", p.getDomain())
          .query("broadcastId", p.getBroadcastId())
          .query("from", p.getFrom())
          .query("to", p.getTo());
    }
    req.query("page", page).query("pageSize", size);
    return core.json(req, opts, EmailList.class);
  }

  /**
   * Every message matching params, page after page.
   *
   * @param params filters, or null; its page is ignored
   * @return the messages
   */
  public PageIterable<EmailRow> all(ListEmailsParams params) {
    return all(params, null);
  }

  /**
   * Every matching message, with per-call options for each page.
   *
   * @param params filters, or null; its page is ignored
   * @param opts per-call options
   * @return the messages
   */
  public PageIterable<EmailRow> all(ListEmailsParams params, RequestOptions opts) {
    int size = params != null && params.getPageSize() != null ? params.getPageSize() : 100;
    return Paging.numbered(
        page -> {
          EmailList res = list(params, page, size, opts);
          List<EmailRow> items = res.getItems();
          int n = items == null ? 0 : items.size();
          return new Paging.Numbered<>(items, n < size || (long) page * size >= res.getTotal());
        });
  }

  /**
   * Counts messages per status.
   *
   * @return the counts
   */
  public EmailStats stats() {
    return stats(null);
  }

  /**
   * Counts messages per status, with per-call options.
   *
   * @param opts per-call options
   * @return the counts
   */
  public EmailStats stats(RequestOptions opts) {
    return core.json(ApiRequest.get("/emails/stats/summary"), opts, EmailStats.class);
  }
}
