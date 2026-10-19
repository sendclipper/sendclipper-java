package com.sendclipper.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.CreateMailboxParams;
import com.sendclipper.model.Mailbox;
import com.sendclipper.model.UpdateMailboxParams;
import java.util.List;

/** Manages addresses on receiving domains. */
public final class Mailboxes {
  private static final TypeReference<List<Mailbox>> LIST = new TypeReference<List<Mailbox>>() {};

  private final HttpCore core;
  private final MailboxMessages messages;

  /**
   * The SDK makes these; use {@code client.mailboxes()}.
   *
   * @param core the engine
   */
  public Mailboxes(HttpCore core) {
    this.core = core;
    this.messages = new MailboxMessages(core);
  }

  /**
   * Mail filed under an address.
   *
   * @return the messages resource
   */
  public MailboxMessages messages() {
    return messages;
  }

  /**
   * The addresses that are not archived.
   *
   * @return the addresses
   */
  public List<Mailbox> list() {
    return list(false, null);
  }

  /**
   * The addresses.
   *
   * @param includeArchived whether to list archived ones too
   * @return the addresses
   */
  public List<Mailbox> list(boolean includeArchived) {
    return list(includeArchived, null);
  }

  /**
   * The addresses, with per-call options.
   *
   * @param includeArchived whether to list archived ones too
   * @param opts per-call options
   * @return the addresses
   */
  public List<Mailbox> list(boolean includeArchived, RequestOptions opts) {
    ApiRequest req = ApiRequest.get("/mailboxes");
    if (includeArchived) {
      req.query("includeArchived", "true");
    }
    return core.json(req, opts, LIST);
  }

  /**
   * One address.
   *
   * @param id the address's id
   * @return the address
   */
  public Mailbox get(String id) {
    return get(id, null);
  }

  /**
   * One address, with per-call options.
   *
   * @param id the address's id
   * @param opts per-call options
   * @return the address
   */
  public Mailbox get(String id, RequestOptions opts) {
    return core.json(ApiRequest.get(path(id)), opts, Mailbox.class);
  }

  /**
   * Makes a new address on a receiving domain.
   *
   * @param params the address
   * @return the address
   */
  public Mailbox create(CreateMailboxParams params) {
    return create(params, null);
  }

  /**
   * Makes an address, with per-call options.
   *
   * @param params the address
   * @param opts per-call options
   * @return the address
   */
  public Mailbox create(CreateMailboxParams params, RequestOptions opts) {
    return core.json(ApiRequest.post("/mailboxes").body(params), opts, Mailbox.class);
  }

  /**
   * Changes the fields set in params.
   *
   * @param id the address's id
   * @param params the changes
   * @return the address
   */
  public Mailbox update(String id, UpdateMailboxParams params) {
    return update(id, params, null);
  }

  /**
   * Changes an address, with per-call options.
   *
   * @param id the address's id
   * @param params the changes
   * @param opts per-call options
   * @return the address
   */
  public Mailbox update(String id, UpdateMailboxParams params, RequestOptions opts) {
    return core.json(ApiRequest.patch(path(id)).body(params), opts, Mailbox.class);
  }

  /**
   * Archives the address. Its mail is kept.
   *
   * @param id the address's id
   * @return the archived address
   */
  public Mailbox archive(String id) {
    return archive(id, null);
  }

  /**
   * Archives the address, with per-call options.
   *
   * @param id the address's id
   * @param opts per-call options
   * @return the archived address
   */
  public Mailbox archive(String id, RequestOptions opts) {
    return core.json(ApiRequest.delete(path(id)), opts, Mailbox.class);
  }

  private static String path(String id) {
    return "/mailboxes/" + ApiRequest.seg(id);
  }
}
