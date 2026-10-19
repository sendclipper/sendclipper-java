package com.sendclipper.resources;

import com.sendclipper.PageIterable;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.core.Paging;
import com.sendclipper.model.DownloadedFile;
import com.sendclipper.model.ListMailboxMessagesParams;
import com.sendclipper.model.MailboxMessage;
import com.sendclipper.model.MailboxMessageList;
import com.sendclipper.model.MailboxMessageRow;

/** Reads mail filed under an address. Reading never marks anything read. */
public final class MailboxMessages {
  private final HttpCore core;

  MailboxMessages(HttpCore core) {
    this.core = core;
  }

  /**
   * One cursor page of an address's mail.
   *
   * @param mailboxId the address's id
   * @param params filters, or null
   * @return the page
   */
  public MailboxMessageList list(String mailboxId, ListMailboxMessagesParams params) {
    return list(mailboxId, params, null);
  }

  /**
   * One cursor page, with per-call options.
   *
   * @param mailboxId the address's id
   * @param params filters, or null
   * @param opts per-call options
   * @return the page
   */
  public MailboxMessageList list(
      String mailboxId, ListMailboxMessagesParams params, RequestOptions opts) {
    return list(mailboxId, params, params == null ? null : params.getCursor(), opts);
  }

  private MailboxMessageList list(
      String mailboxId, ListMailboxMessagesParams p, String cursor, RequestOptions opts) {
    ApiRequest req = ApiRequest.get(path(mailboxId) + "/messages");
    if (p != null) {
      req.query("folder", p.getFolder())
          .query("search", p.getSearch())
          .query("label", p.getLabel())
          .query("limit", p.getLimit());
    }
    req.query("cursor", cursor);
    return core.json(req, opts, MailboxMessageList.class);
  }

  /**
   * An address's mail, following the cursor.
   *
   * @param mailboxId the address's id
   * @param params filters, or null; its cursor is ignored
   * @return the messages
   */
  public PageIterable<MailboxMessageRow> all(String mailboxId, ListMailboxMessagesParams params) {
    return all(mailboxId, params, null);
  }

  /**
   * An address's mail, with per-call options for each page.
   *
   * @param mailboxId the address's id
   * @param params filters, or null; its cursor is ignored
   * @param opts per-call options
   * @return the messages
   */
  public PageIterable<MailboxMessageRow> all(
      String mailboxId, ListMailboxMessagesParams params, RequestOptions opts) {
    return Paging.cursor(
        cursor -> {
          MailboxMessageList res = list(mailboxId, params, cursor, opts);
          return new Paging.Cursored<>(res.getItems(), res.getPage());
        });
  }

  /**
   * One message in full, with its conversation.
   *
   * @param mailboxId the address's id
   * @param messageId the message's id
   * @return the message
   */
  public MailboxMessage get(String mailboxId, String messageId) {
    return get(mailboxId, messageId, null);
  }

  /**
   * One message, with per-call options.
   *
   * @param mailboxId the address's id
   * @param messageId the message's id
   * @param opts per-call options
   * @return the message
   */
  public MailboxMessage get(String mailboxId, String messageId, RequestOptions opts) {
    return core.json(
        ApiRequest.get(path(mailboxId) + "/messages/" + ApiRequest.seg(messageId)),
        opts,
        MailboxMessage.class);
  }

  /**
   * Downloads one attachment.
   *
   * @param mailboxId the address's id
   * @param messageId the message's id
   * @param attachmentId the attachment's id
   * @return the file
   */
  public DownloadedFile attachment(String mailboxId, String messageId, String attachmentId) {
    return attachment(mailboxId, messageId, attachmentId, null);
  }

  /**
   * Downloads one attachment, with per-call options.
   *
   * @param mailboxId the address's id
   * @param messageId the message's id
   * @param attachmentId the attachment's id
   * @param opts per-call options
   * @return the file
   */
  public DownloadedFile attachment(
      String mailboxId, String messageId, String attachmentId, RequestOptions opts) {
    return core.bytes(
        ApiRequest.get(
                path(mailboxId)
                    + "/messages/"
                    + ApiRequest.seg(messageId)
                    + "/parts/"
                    + ApiRequest.seg(attachmentId))
            .query("download", "1"),
        opts);
  }

  private static String path(String mailboxId) {
    return "/mailboxes/" + ApiRequest.seg(mailboxId);
  }
}
