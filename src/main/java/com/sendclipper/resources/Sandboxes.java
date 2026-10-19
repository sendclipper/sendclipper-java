package com.sendclipper.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.ResetSandboxCredentials;
import com.sendclipper.model.Sandbox;
import com.sendclipper.model.SandboxCredentials;
import com.sendclipper.model.SandboxParams;
import com.sendclipper.model.SandboxSendResult;
import com.sendclipper.model.SendEmailParams;
import java.util.List;

/** Manages sandboxes: inboxes that trap mail while you develop. */
public final class Sandboxes {
  private static final TypeReference<List<Sandbox>> LIST = new TypeReference<List<Sandbox>>() {};

  private static final class Removed {
    int deletedMessages;
  }

  private final HttpCore core;
  private final SandboxMessages messages;

  /**
   * The SDK makes these; use {@code client.sandboxes()}.
   *
   * @param core the engine
   */
  public Sandboxes(HttpCore core) {
    this.core = core;
    this.messages = new SandboxMessages(core);
  }

  /**
   * What the sandboxes trapped.
   *
   * @return the messages resource
   */
  public SandboxMessages messages() {
    return messages;
  }

  /**
   * Every sandbox.
   *
   * @return the sandboxes
   */
  public List<Sandbox> list() {
    return list(null);
  }

  /**
   * Every sandbox, with per-call options.
   *
   * @param opts per-call options
   * @return the sandboxes
   */
  public List<Sandbox> list(RequestOptions opts) {
    return core.json(ApiRequest.get("/sandboxes"), opts, LIST);
  }

  /**
   * One sandbox.
   *
   * @param id the sandbox's id
   * @return the sandbox
   */
  public Sandbox get(String id) {
    return get(id, null);
  }

  /**
   * One sandbox, with per-call options.
   *
   * @param id the sandbox's id
   * @param opts per-call options
   * @return the sandbox
   */
  public Sandbox get(String id, RequestOptions opts) {
    return core.json(ApiRequest.get(path(id)), opts, Sandbox.class);
  }

  /**
   * Makes a sandbox. The name is required.
   *
   * @param params the sandbox
   * @return the sandbox
   */
  public Sandbox create(SandboxParams params) {
    return create(params, null);
  }

  /**
   * Makes a sandbox, with per-call options.
   *
   * @param params the sandbox
   * @param opts per-call options
   * @return the sandbox
   */
  public Sandbox create(SandboxParams params, RequestOptions opts) {
    return core.json(ApiRequest.post("/sandboxes").body(params), opts, Sandbox.class);
  }

  /**
   * Renames a sandbox. Renaming never changes the address or the credentials.
   *
   * @param id the sandbox's id
   * @param params the changes
   * @return the sandbox
   */
  public Sandbox update(String id, SandboxParams params) {
    return update(id, params, null);
  }

  /**
   * Renames a sandbox, with per-call options.
   *
   * @param id the sandbox's id
   * @param params the changes
   * @param opts per-call options
   * @return the sandbox
   */
  public Sandbox update(String id, SandboxParams params, RequestOptions opts) {
    return core.json(ApiRequest.patch(path(id)).body(params), opts, Sandbox.class);
  }

  /**
   * Deletes the sandbox and every message in it.
   *
   * @param id the sandbox's id
   * @return how many messages went
   */
  public int remove(String id) {
    return remove(id, null);
  }

  /**
   * Deletes the sandbox, with per-call options.
   *
   * @param id the sandbox's id
   * @param opts per-call options
   * @return how many messages went
   */
  public int remove(String id, RequestOptions opts) {
    Removed r = core.json(ApiRequest.delete(path(id)), opts, Removed.class);
    return r == null ? 0 : r.deletedMessages;
  }

  /**
   * The SMTP host, port, username and password to point a project at.
   *
   * @param id the sandbox's id
   * @return the credentials
   */
  public SandboxCredentials credentials(String id) {
    return credentials(id, null);
  }

  /**
   * The credentials, with per-call options.
   *
   * @param id the sandbox's id
   * @param opts per-call options
   * @return the credentials
   */
  public SandboxCredentials credentials(String id, RequestOptions opts) {
    return core.json(ApiRequest.get(path(id) + "/credentials"), opts, SandboxCredentials.class);
  }

  /**
   * Replaces BOTH the SMTP password and the {@code sc_sbx_} token. The token is shown only here.
   *
   * @param id the sandbox's id
   * @return the new credentials
   */
  public ResetSandboxCredentials resetCredentials(String id) {
    return resetCredentials(id, null);
  }

  /**
   * Replaces the credentials, with per-call options.
   *
   * @param id the sandbox's id
   * @param opts per-call options
   * @return the new credentials
   */
  public ResetSandboxCredentials resetCredentials(String id, RequestOptions opts) {
    return core.json(
        ApiRequest.post(path(id) + "/credentials"), opts, ResetSandboxCredentials.class);
  }

  /**
   * Sends to a sandbox through its JSON door, with the sandbox's own {@code sc_sbx_} token. Same
   * fields as {@code emails().send(...)}; nothing is delivered.
   *
   * @param sandboxToken the sandbox's token
   * @param params the message
   * @return the trapped message's id
   */
  public SandboxSendResult send(String sandboxToken, SendEmailParams params) {
    return send(sandboxToken, params, null);
  }

  /**
   * Sends to a sandbox, with per-call options.
   *
   * @param sandboxToken the sandbox's token
   * @param params the message
   * @param opts per-call options
   * @return the trapped message's id
   */
  public SandboxSendResult send(String sandboxToken, SendEmailParams params, RequestOptions opts) {
    return core.json(
        ApiRequest.post("/sandbox/emails").body(params).bearer(sandboxToken),
        opts,
        SandboxSendResult.class);
  }

  private static String path(String id) {
    return "/sandboxes/" + ApiRequest.seg(id);
  }
}
