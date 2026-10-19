package com.sendclipper.resources;

import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.SendBatchResponse;
import com.sendclipper.model.SendEmailParams;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Sends up to 500 messages in one request. */
public final class Batch {
  private final HttpCore core;

  /**
   * The SDK makes these; use {@code client.batch()}.
   *
   * @param core the engine
   */
  public Batch(HttpCore core) {
    this.core = core;
  }

  /**
   * Sends up to 500 messages (one recipient each). Every item is accepted or refused on its own; a
   * refused item costs its siblings nothing.
   *
   * @param emails the messages
   * @return one result per item, in order
   */
  public SendBatchResponse send(List<SendEmailParams> emails) {
    return send(core, emails, null);
  }

  /**
   * Sends a batch, with per-call options.
   *
   * @param emails the messages
   * @param opts per-call options
   * @return one result per item, in order
   */
  public SendBatchResponse send(List<SendEmailParams> emails, RequestOptions opts) {
    return send(core, emails, opts);
  }

  static SendBatchResponse send(HttpCore core, List<SendEmailParams> emails, RequestOptions opts) {
    List<SendEmailParams> items = emails == null ? Collections.emptyList() : emails;
    Map<String, Object> body = Collections.singletonMap("emails", items);
    return core.json(ApiRequest.post("/emails/batch").body(body), opts, SendBatchResponse.class);
  }
}
