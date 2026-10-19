package com.sendclipper.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sendclipper.RequestOptions;
import com.sendclipper.SendClipperException;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.ListSandboxMessagesParams;
import com.sendclipper.model.SandboxMessage;
import com.sendclipper.model.SandboxMessageSummary;
import com.sendclipper.model.WaitForOptions;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

/** Reads what a sandbox trapped. */
public final class SandboxMessages {
  private static final TypeReference<List<SandboxMessageSummary>> LIST =
      new TypeReference<List<SandboxMessageSummary>>() {};

  private static final class Deleted {
    int deleted;
  }

  private static final class Updated {
    int updated;
  }

  private final HttpCore core;

  SandboxMessages(HttpCore core) {
    this.core = core;
  }

  /**
   * A sandbox's messages, newest first.
   *
   * @param sandboxId the sandbox's id
   * @param params filters, or null
   * @return the messages
   */
  public List<SandboxMessageSummary> list(String sandboxId, ListSandboxMessagesParams params) {
    return list(sandboxId, params, null);
  }

  /**
   * A sandbox's messages, with per-call options.
   *
   * @param sandboxId the sandbox's id
   * @param params filters, or null
   * @param opts per-call options
   * @return the messages
   */
  public List<SandboxMessageSummary> list(
      String sandboxId, ListSandboxMessagesParams params, RequestOptions opts) {
    ApiRequest req = ApiRequest.get(path(sandboxId) + "/messages");
    if (params != null) {
      req.query("search", params.getSearch()).query("limit", params.getLimit());
    }
    return core.json(req, opts, LIST);
  }

  /**
   * One trapped message in full.
   *
   * @param sandboxId the sandbox's id
   * @param messageId the message's id
   * @return the message
   */
  public SandboxMessage get(String sandboxId, String messageId) {
    return get(sandboxId, messageId, null);
  }

  /**
   * One trapped message, with per-call options.
   *
   * @param sandboxId the sandbox's id
   * @param messageId the message's id
   * @param opts per-call options
   * @return the message
   */
  public SandboxMessage get(String sandboxId, String messageId, RequestOptions opts) {
    return core.json(ApiRequest.get(message(sandboxId, messageId)), opts, SandboxMessage.class);
  }

  /**
   * The message's raw MIME source.
   *
   * @param sandboxId the sandbox's id
   * @param messageId the message's id
   * @return the source
   */
  public String raw(String sandboxId, String messageId) {
    return raw(sandboxId, messageId, null);
  }

  /**
   * The raw source, with per-call options.
   *
   * @param sandboxId the sandbox's id
   * @param messageId the message's id
   * @param opts per-call options
   * @return the source
   */
  public String raw(String sandboxId, String messageId, RequestOptions opts) {
    return core.text(ApiRequest.get(message(sandboxId, messageId) + "/raw"), opts);
  }

  /**
   * Deletes one trapped message.
   *
   * @param sandboxId the sandbox's id
   * @param messageId the message's id
   */
  public void remove(String sandboxId, String messageId) {
    remove(sandboxId, messageId, null);
  }

  /**
   * Deletes one message, with per-call options.
   *
   * @param sandboxId the sandbox's id
   * @param messageId the message's id
   * @param opts per-call options
   */
  public void remove(String sandboxId, String messageId, RequestOptions opts) {
    core.discard(ApiRequest.delete(message(sandboxId, messageId)), opts);
  }

  /**
   * Empties the sandbox.
   *
   * @param sandboxId the sandbox's id
   * @return how many messages went
   */
  public int clear(String sandboxId) {
    return clear(sandboxId, null);
  }

  /**
   * Empties the sandbox, with per-call options.
   *
   * @param sandboxId the sandbox's id
   * @param opts per-call options
   * @return how many messages went
   */
  public int clear(String sandboxId, RequestOptions opts) {
    Deleted d = core.json(ApiRequest.delete(path(sandboxId) + "/messages"), opts, Deleted.class);
    return d == null ? 0 : d.deleted;
  }

  /**
   * Marks every message read.
   *
   * @param sandboxId the sandbox's id
   * @return how many changed
   */
  public int markAllRead(String sandboxId) {
    return markAllRead(sandboxId, null);
  }

  /**
   * Marks every message read, with per-call options.
   *
   * @param sandboxId the sandbox's id
   * @param opts per-call options
   * @return how many changed
   */
  public int markAllRead(String sandboxId, RequestOptions opts) {
    Updated u = core.json(ApiRequest.post(path(sandboxId) + "/read-all"), opts, Updated.class);
    return u == null ? 0 : u.updated;
  }

  /**
   * Polls until a message trapped after this call started arrives, then returns it in full.
   * Handy in end-to-end tests. Waits at most 30 seconds.
   *
   * @param sandboxId the sandbox's id
   * @return the message
   * @throws SendClipperException named {@code timeout} when nothing arrived in time
   */
  public SandboxMessage waitFor(String sandboxId) {
    return waitFor(sandboxId, null);
  }

  /**
   * Polls until a message that {@code options.getMatch()} accepts arrives, then returns it in
   * full.
   *
   * @param sandboxId the sandbox's id
   * @param options what to wait for, or null for the defaults
   * @return the message
   * @throws SendClipperException named {@code timeout} when nothing matched in time
   */
  public SandboxMessage waitFor(String sandboxId, WaitForOptions options) {
    WaitForOptions o = options == null ? WaitForOptions.builder().build() : options;
    Instant started = Instant.now();
    Predicate<SandboxMessageSummary> match = o.getMatch();
    if (match == null) {
      Instant since = started.minusSeconds(1);
      match = m -> m.getTrappedAt() != null && !m.getTrappedAt().isBefore(since);
    }
    Duration interval =
        o.getInterval() == null || o.getInterval().isZero() || o.getInterval().isNegative()
            ? Duration.ofSeconds(1)
            : o.getInterval();
    Instant deadline = started.plus(o.getTimeout() == null ? Duration.ofSeconds(30) : o.getTimeout());
    ListSandboxMessagesParams page = ListSandboxMessagesParams.builder().limit(50).build();
    while (true) {
      for (SandboxMessageSummary m : list(sandboxId, page)) {
        if (match.test(m)) {
          return get(sandboxId, m.getId());
        }
      }
      Duration left = Duration.between(Instant.now(), deadline);
      if (left.isNegative() || left.isZero()) {
        Duration took = Duration.between(started, Instant.now());
        throw new SendClipperException(
            "timeout",
            "No matching message reached sandbox " + sandboxId + " within " + took.toMillis() + " ms.",
            0,
            null,
            null,
            null);
      }
      try {
        Thread.sleep(Math.min(interval.toMillis(), Math.max(1, left.toMillis())));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new SendClipperException(
            "network_error", "The wait was interrupted.", 0, null, null, e);
      }
    }
  }

  private static String path(String sandboxId) {
    return "/sandboxes/" + ApiRequest.seg(sandboxId);
  }

  private static String message(String sandboxId, String messageId) {
    return path(sandboxId) + "/messages/" + ApiRequest.seg(messageId);
  }
}
